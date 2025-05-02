package com.application.test.Model;

import com.application.test.Model.DictionaryEntry;
import java.util.List;
import java.util.Optional;

public interface DictionarySource {
    String getSourceId();
    String getDisplayName();

    void loadData();
    void saveData();

    boolean addEntry(DictionaryEntry entry);
    boolean updateEntry(String oldHeadword, DictionaryEntry updatedEntry);
    boolean deleteEntry(String headword);

    Optional<DictionaryEntry> lookupEntry(String headword);
    List<DictionaryEntry> searchEntriesByPrefix(String prefix);

    List<DictionaryEntry> getAllEntries();
}
