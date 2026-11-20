package edu.arizona.documenteditor.macro;

import java.util.LinkedHashMap;

/** Records and stores named macros. */
public final class MacroManager {

    private final LinkedHashMap<String, Macro> macros;
    private Macro recording;

    /** Creates a manager with no saved or active macros. */
    public MacroManager() {
        macros = new LinkedHashMap<String, Macro>();
        recording = null;
    }

    /**
     * Starts recording a new macro.
     *
     * @param name the macro name
     * @throws IllegalStateException if recording is already active
     * @throws IllegalArgumentException if the name is invalid
     *         or already exists
     */
    public void startRecording(String name) {
        if (recording != null) {
            throw new IllegalStateException("A macro is already being recorded.");
        }

        validateName(name);

        if (macros.containsKey(name)) {
            throw new IllegalArgumentException("Macro already exists. Use a different name.");
        }

        recording = new Macro(name);
    }

    /**
     * Records a command during an active recording.
     *
     * @param line the complete command line
     * @throws IllegalStateException if recording is not active
     * @throws IllegalArgumentException if the command is invalid
     */
    public void record(String line) {
        if (recording == null) {
            throw new IllegalStateException("No macro is being recorded.");
        }

        recording.addCommand(line);
    }

    /**
     * Saves the active recording and stops recording.
     *
     * @throws IllegalStateException if recording is not active
     */
    public void stopRecording() {
        if (recording == null) {
            throw new IllegalStateException("No macro is being recorded.");
        }

        String name = recording.getName();
        macros.put(name, recording);

        recording = null;
    }

    /**
     * Reports whether recording is active.
     *
     * @return true if a macro is being recorded
     */
    public boolean isRecording() {
        return recording != null;
    }

    /**
     * Retrieves a saved macro as an independent copy.
     *
     * @param name the macro name
     * @return a copy of the saved macro
     * @throws IllegalArgumentException if the name is invalid
     *         or the macro does not exist
     */
    public Macro getMacro(String name) {
        validateName(name);

        Macro saved = macros.get(name);

        if (saved == null) {
            throw new IllegalArgumentException("Macro does not exist: " + name);
        }

        Macro copy = new Macro(saved.getName());
        java.util.ArrayList<String> commands = saved.getCommands();

        for (int i = 0; i < commands.size(); i++) {
            copy.addCommand(commands.get(i));
        }

        return copy;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Macro name cannot be empty.");
        }
    }
}