package com.example.djabko_journal;

public class JournalConstants {
    public static class JournalConstant {
        public final String raw, label;

        public JournalConstant(String raw, String label) {
            this.raw = raw;
            this.label = label;
        }
    }

    public static final JournalConstant NOTEBOOK = new JournalConstant("notebook", "Notebook");
    public static final JournalConstant MESSAGE = new JournalConstant("message", "Message");
    public static final JournalConstant DATETIME = new JournalConstant("datetime", "Datetime");
    public static final JournalConstant AUTHOR = new JournalConstant("author", "Author");
    public static final JournalConstant TAG1 = new JournalConstant("tag1", "Tag 1");
    public static final JournalConstant TAG2 = new JournalConstant("tag2", "Tag 2");
    public static final JournalConstant TAG3 = new JournalConstant("tag3", "Tag 3");
    public static final JournalConstant TAG4 = new JournalConstant("tag4", "Tag 4");
}