import java.util.LinkedHashMap;

public class MacroManager {

    LinkedHashMap<String, Macro> Macros;
    public Macro Recording;

    public MacroManager() {
        Macros = new LinkedHashMap<>();
        Recording = new Macro();
    }

    public void startRecording(String name) {

        Recording = new Macro(name);
    }

    public void record(String line) {
        Recording.addCommand(line);
    }

    public void stopRecording() {

        Macros.put(Recording.name,Recording);
        Recording = null;
    }

    public Macro getMacro(String name) {

        return Macros.get(name);
    }
}
