package songbook.index;

/**
 * Created by laurent on 14/05/2014.
 */
public class Directive {

    public final String name;

    public final String value;

    public Directive(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Directive{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
