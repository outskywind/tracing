package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.RenameUtil;

import java.util.Date;

/**
 * Created by Caedmon on 2017/3/30.
 */
public class KLogTimeBasedRollingPolicy<E>  extends TimeBasedRollingPolicy<E> {
    private String serviceName;
    private String addr;
    private RenameUtil klogRenameUtil=new RenameUtil();
    private ArchiveRemover klogRemover;
    public KLogTimeBasedRollingPolicy(String serviceName, String addr){
        this.serviceName=serviceName;
        this.addr=addr;

    }

    @Override
    public void start() {
        super.start();
        if (getMaxHistory() != INFINITE_HISTORY) {
            klogRemover = timeBasedFileNamingAndTriggeringPolicy.getArchiveRemover();
            klogRemover.setMaxHistory(getMaxHistory());
            if(cleanHistoryOnStart) {
                addInfo("Cleaning on start up");
                klogRemover.clean(new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
            }
        }
        klogRenameUtil.setContext(this.getContext());
    }

    @Override
    public void rollover() throws RolloverFailure {
        String elapsedPeriodsFileName = getTimeBasedFileNamingAndTriggeringPolicy()
                .getElapsedPeriodsFileName();
        elapsedPeriodsFileName=elapsedPeriodsFileName
                .replaceAll("%PARSER_ERROR\\[sn\\]",this.serviceName)
                .replaceAll("%PARSER_ERROR\\[addr\\]",this.addr);
        String elapsedPeriodStem = FileFilterUtil.afterLastSlash(elapsedPeriodsFileName);
        if (compressionMode == CompressionMode.NONE) {
            if (getParentsRawFileProperty() != null) {
                klogRenameUtil.rename(getParentsRawFileProperty(), elapsedPeriodsFileName);
            } // else { nothing to do if CompressionMode == NONE and parentsRawFileProperty == null }
        } else {
            if (getParentsRawFileProperty() == null) {
                future = asyncCompress(elapsedPeriodsFileName, elapsedPeriodsFileName, elapsedPeriodStem);
            } else {
                future = renamedRawAndAsyncCompress(elapsedPeriodsFileName, elapsedPeriodStem);
            }
        }
        if (klogRemover != null) {
            klogRemover.clean(new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
        }
    }
}
