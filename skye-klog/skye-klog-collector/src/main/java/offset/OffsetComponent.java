package offset;

/**
 * Created by Caedmon on 2017/3/2.
 */
public interface OffsetComponent {

    void setOffsetConfig(OffsetConfig offsetConfig);

    void setOffset(long offset);

    long getOffset();
}
