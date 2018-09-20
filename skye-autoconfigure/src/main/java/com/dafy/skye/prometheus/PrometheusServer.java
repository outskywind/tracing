package com.dafy.skye.prometheus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

/**
 * 这个类在 <code>simpleclient_httpserver</code> 依赖中类 <code>io.prometheus.client.exporter.HTTPServer</code> 的基础上
 * 做了少许修改，这样做是因为类 <code>HTTPServer</code> 只会使用 <b>/</b> 和 <b>/metrics</b> 作为<b>context path</b>，而且不
 * 支持自定义。<p>
 *
 * Created by quanchengyun on 2018/9/14.
 * @see io.prometheus.client.exporter.HTTPServer
 */
public class PrometheusServer {

    protected final HttpServer server;
    protected final ExecutorService executorService;

    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        protected ByteArrayOutputStream initialValue()
        {
            return new ByteArrayOutputStream(1 << 20);
        }
    }

    static class HTTPMetricHandler implements HttpHandler {
        private CollectorRegistry registry;
        private final LocalByteArray response = new LocalByteArray();

        HTTPMetricHandler(CollectorRegistry registry) {
            this.registry = registry;
        }


        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getRawQuery();

            ByteArrayOutputStream response = this.response.get();
            response.reset();
            OutputStreamWriter osw = new OutputStreamWriter(response);
            TextFormat.write004(osw,
                    registry.filteredMetricFamilySamples(parseQuery(query)));
            osw.flush();
            osw.close();
            response.flush();
            response.close();

            t.getResponseHeaders().set("Content-Type",
                    TextFormat.CONTENT_TYPE_004);
            t.getResponseHeaders().set("Content-Length",
                    String.valueOf(response.size()));
            if (shouldUseCompression(t)) {
                t.getResponseHeaders().set("Content-Encoding", "gzip");
                t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                final GZIPOutputStream os = new GZIPOutputStream(t.getResponseBody());
                response.writeTo(os);
                os.finish();
            } else {
                t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.size());
                response.writeTo(t.getResponseBody());
            }
            t.close();
        }

    }

    protected static boolean shouldUseCompression(HttpExchange exchange) {
        List<String> encodingHeaders = exchange.getRequestHeaders().get("Accept-Encoding");
        if (encodingHeaders == null) return false;

        for (String encodingHeader : encodingHeaders) {
            String[] encodings = encodingHeader.split(",");
            for (String encoding : encodings) {
                if (encoding.trim().toLowerCase().equals("gzip")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static Set<String> parseQuery(String query) throws IOException {
        Set<String> names = new HashSet<String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
    }


    static class DaemonThreadFactory implements ThreadFactory {
        private ThreadFactory delegate;
        private final boolean daemon;

        DaemonThreadFactory(ThreadFactory delegate, boolean daemon) {
            this.delegate = delegate;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setDaemon(daemon);
            return t;
        }

        static ThreadFactory defaultThreadFactory(boolean daemon) {
            return new DaemonThreadFactory(Executors.defaultThreadFactory(), daemon);
        }
    }


    /**
     * Start a HTTP server serving Prometheus metrics from the given registry.
     */
    public PrometheusServer(InetSocketAddress addr, CollectorRegistry registry, boolean daemon, String contextPath) throws IOException {
        server = HttpServer.create();
        server.bind(addr, 3);
        HttpHandler mHandler = new HTTPMetricHandler(registry);
        server.createContext("/", mHandler);
        server.createContext(contextPath, mHandler);
        executorService = Executors.newFixedThreadPool(5, DaemonThreadFactory.defaultThreadFactory(daemon));
        server.setExecutor(executorService);
        start(daemon);
    }

    /**
     * Start a HTTP server serving Prometheus metrics from the given registry using non-daemon threads.
     */
    public PrometheusServer(InetSocketAddress addr, CollectorRegistry registry,String contextPath) throws IOException {
        this(addr, registry, false,contextPath);
    }

    /**
     * Start a HTTP server serving the default Prometheus registry.
     */
    public PrometheusServer(int port, boolean daemon,String contextPath) throws IOException {
        this(new InetSocketAddress(port), CollectorRegistry.defaultRegistry, daemon,contextPath);
    }

    /**
     * Start a HTTP server serving the default Prometheus registry using non-daemon threads.
     */
    public PrometheusServer(int port,String contextPath) throws IOException {
        this(port, false,contextPath);
    }

    /**
     * Start a HTTP server serving the default Prometheus registry.
     */
    public PrometheusServer(String host, int port, boolean daemon,String contextPath) throws IOException {
        this(new InetSocketAddress(host, port), CollectorRegistry.defaultRegistry, daemon,contextPath);
    }

    /**
     * Start a HTTP server serving the default Prometheus registry using non-daemon threads.
     */
    public PrometheusServer(String host, int port,String contextPath) throws IOException {
        this(new InetSocketAddress(host, port), CollectorRegistry.defaultRegistry, false,contextPath);
    }

    /**
     * Start a HTTP server by making sure that its background thread inherit proper daemon flag.
     */
    private void start(boolean daemon) {
        if (daemon == Thread.currentThread().isDaemon()) {
            server.start();
        } else {
            FutureTask<Void> startTask = new FutureTask<Void>(new Runnable() {
                @Override
                public void run() {
                    server.start();
                }
            }, null);
            DaemonThreadFactory.defaultThreadFactory(daemon).newThread(startTask).start();
            try {
                startTask.get();
            } catch (ExecutionException e) {
                throw new RuntimeException("Unexpected exception on starting HTTPSever", e);
            } catch (InterruptedException e) {
                // This is possible only if the current tread has been interrupted,
                // but in real use cases this should not happen.
                // In any case, there is nothing to do, except to propagate interrupted flag.
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Stop the HTTP server.
     */
    public void stop() {
        server.stop(0);
        executorService.shutdown(); // Free any (parked/idle) threads in pool
    }
}
