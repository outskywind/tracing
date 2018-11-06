package com.dafy.skye.zipkin;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import zipkin2.Call;
import zipkin2.Callback;
import zipkin2.CheckResult;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AwaitableCallback;
import zipkin2.reporter.BytesMessageEncoder;
import zipkin2.reporter.Sender;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by quanchengyun on 2018/10/15.
 */
public class KafkaSender10 extends Sender {
    /** Creates a sender that sends {@link Encoding#JSON} messages. */
    public static KafkaSender10 create(String bootstrapServers) {
        return newBuilder().bootstrapServers(bootstrapServers).build();
    }

    public static Builder newBuilder() {
        // Settings below correspond to "Producer Configs"
        // http://kafka.apache.org/0102/documentation.html#producerconfigs
        Properties properties = new Properties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ByteArraySerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "0");
        return new Builder(properties);
    }

    /** Configuration including defaults needed to send spans to a Kafka topic. */
    public static final class Builder {
        final Properties properties;
        Encoding encoding = Encoding.JSON;
        String topic = "zipkin";
        int messageMaxBytes = 1000000;

        Builder(Properties properties) {
            this.properties = properties;
        }

        Builder(KafkaSender10 sender) {
            properties = new Properties();
            properties.putAll(sender.properties);
            encoding = sender.encoding;
            topic = sender.topic;
            messageMaxBytes = sender.messageMaxBytes;
        }

        /** Topic zipkin spans will be send to. Defaults to "zipkin" */
        public Builder topic(String topic) {
            if (topic == null) throw new NullPointerException("topic == null");
            this.topic = topic;
            return this;
        }

        /**
         * Initial set of kafka servers to connect to, rest of cluster will be discovered (comma
         * separated). Ex "192.168.99.100:9092" No default
         *
         * @see ProducerConfig#BOOTSTRAP_SERVERS_CONFIG
         */
        public final Builder bootstrapServers(String bootstrapServers) {
            if (bootstrapServers == null) throw new NullPointerException("bootstrapServers == null");
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            return this;
        }

        /**
         * Maximum size of a message. Must be equal to or less than the server's "message.max.bytes".
         * Default 1000000.
         */
        public Builder messageMaxBytes(int messageMaxBytes) {
            this.messageMaxBytes = messageMaxBytes;
            return this;
        }

        /**
         * By default, a producer will be created, targeted to {@link #bootstrapServers(String)} with 0
         * required {@link ProducerConfig#ACKS_CONFIG acks}. Any properties set here will affect the
         * producer config.
         *
         * <p>For example: Reduce the timeout blocking from one minute to 5 seconds.
         * <pre>{@code
         * Map<String, String> overrides = new LinkedHashMap<>();
         * overrides.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000");
         * builder.overrides(overrides);
         * }</pre>
         *
         * @see ProducerConfig
         */
        public final Builder overrides(Map<String, ?> overrides) {
            if (overrides == null) throw new NullPointerException("overrides == null");
            properties.putAll(overrides);
            return this;
        }

        /**
         * Use this to change the encoding used in messages. Default is {@linkplain Encoding#JSON}
         *
         * <p>Note: If ultimately sending to Zipkin, version 2.8+ is required to process protobuf.
         */
        public Builder encoding(Encoding encoding) {
            if (encoding == null) throw new NullPointerException("encoding == null");
            this.encoding = encoding;
            return this;
        }

        public KafkaSender10 build() {
            return new KafkaSender10(this);
        }
    }

    final Properties properties;
    final String topic;
    final Encoding encoding;
    final BytesMessageEncoder encoder;
    final int messageMaxBytes;

    KafkaSender10(Builder builder) {
        properties = new Properties();
        properties.putAll(builder.properties);
        topic = builder.topic;
        encoding = builder.encoding;
        encoder = BytesMessageEncoder.forEncoding(builder.encoding);
        messageMaxBytes = builder.messageMaxBytes;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /** get and close are typically called from different threads */
    volatile KafkaProducer<byte[], byte[]> producer;
    volatile boolean closeCalled;
    //volatile AdminClient adminClient;

    @Override public int messageSizeInBytes(List<byte[]> encodedSpans) {
        return encoding.listSizeInBytes(encodedSpans);
    }

    @Override public int messageSizeInBytes(int encodedSizeInBytes) {
        return encoding.listSizeInBytes(encodedSizeInBytes);
    }

    @Override public Encoding encoding() {
        return encoding;
    }

    @Override public int messageMaxBytes() {
        return messageMaxBytes;
    }

    /**
     * This sends all of the spans as a single message.
     *
     * <p>NOTE: this blocks until the metadata server is available.
     */
    @Override public zipkin2.Call<Void> sendSpans(List<byte[]> encodedSpans) {
        if (closeCalled) throw new IllegalStateException("closed");
        byte[] message = encoder.encode(encodedSpans);
        return new KafkaCall(message);
    }

    /** Ensures there are no problems reading metadata about the topic. */
    @Override public CheckResult check() {
        try {
            //KafkaFuture<String> maybeClusterId = getAdminClient().describeCluster().clusterId();
            //maybeClusterId.get(1, TimeUnit.SECONDS);
            get().partitionsFor(topic);
            return CheckResult.OK;
        } catch (Exception e) {
            return CheckResult.failed(e);
        }
    }

    KafkaProducer<byte[], byte[]> get() {
        if (producer == null) {
            synchronized (this) {
                if (producer == null) {
                    producer = new KafkaProducer<>(properties);
                }
            }
        }
        return producer;
    }


    /*AdminClient getAdminClient() {
        if (adminClient == null) {
            synchronized (this) {
                if (adminClient == null) {
                    adminClient = AdminClient.create(properties);
                }
            }
        }
        return adminClient;
    }*/

    @Override public synchronized void close() {
        if (closeCalled) return;
        KafkaProducer<byte[], byte[]>  producer = this.producer;
        if (producer != null) producer.close();
        //AdminClient adminClient = this.adminClient;
        //if (adminClient != null) adminClient.close(1, TimeUnit.SECONDS);
        closeCalled = true;
    }

    @Override public final String toString() {
        return "KafkaSender{"
                + "bootstrapServers=" + properties.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)
                + ", topic=" + topic
                + "}";
    }

    class KafkaCall extends Call.Base<Void> { // KafkaFuture is not cancelable
        private final byte[] message;

        KafkaCall(byte[] message) {
            this.message = message;
        }

        @Override protected Void doExecute() throws IOException {
            AwaitableCallback callback = new AwaitableCallback();
            get().send(new ProducerRecord<byte[],byte[]>(topic, message), new CallbackAdapter(callback));
            callback.await();
            return null;
        }

        @Override protected void doEnqueue(Callback<Void> callback) {
            get().send(new ProducerRecord<byte[],byte[]>(topic, message), new CallbackAdapter(callback));
        }

        @Override public Call<Void> clone() {
            return new KafkaCall(message);
        }
    }

    static final class CallbackAdapter implements org.apache.kafka.clients.producer.Callback {
        final Callback<Void> delegate;

        CallbackAdapter(Callback<Void> delegate) {
            this.delegate = delegate;
        }

        @Override public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception == null) {
                delegate.onSuccess(null);
            } else {
                delegate.onError(exception);
            }
        }

        @Override public String toString() {
            return delegate.toString();
        }
    }



}
