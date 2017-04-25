package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.RenameUtil;

import java.util.Date;

/**
 * Created by Caedmon on 2017/3/30.
 */
public class SkyeLogTimeBasedRollingPolicy<E>  extends TimeBasedRollingPolicy<E> {
    private String serviceName;
    private String addr;
    private RenameUtil skyeRenameUtil =new RenameUtil();
    private ArchiveRemover skyeRemover;
    public SkyeLogTimeBasedRollingPolicy(String serviceName, String addr){
        this.serviceName=serviceName;
        this.addr=addr;

    }

    @Override
    public void start() {
        super.start();
        if (getMaxHistory() != INFINITE_HISTORY) {
            skyeRemover = timeBasedFileNamingAndTriggeringPolicy.getArchiveRemover();
            skyeRemover.setMaxHistory(getMaxHistory());
            if(cleanHistoryOnStart) {
                addInfo("Cleaning on start up");
                skyeRemover.clean(new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
            }
        }
        skyeRenameUtil.setContext(this.getContext());
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
                skyeRenameUtil.rename(getParentsRawFileProperty(), elapsedPeriodsFileName);
            } // else { nothing to do if CompressionMode == NONE and parentsRawFileProperty == null }
        } else {
            if (getParentsRawFileProperty() == null) {
                future = asyncCompress(elapsedPeriodsFileName, elapsedPeriodsFileName, elapsedPeriodStem);
            } else {
                future = renamedRawAndAsyncCompress(elapsedPeriodsFileName, elapsedPeriodStem);
            }
        }
        if (skyeRemover != null) {
            skyeRemover.clean(new Date(timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
        }
    }
}
