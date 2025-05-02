package com.application.test.Model;
import com.application.test.Model.DictionarySource;
import com.application.test.Model.EngVieManagement;
import com.application.test.Model.VieEngManagement;

import java.util.*;

public class GeneralManagement {
    // Map lưu trữ các nguồn từ điển theo SourceId
    private final Map<String, DictionarySource> sources;

    // Source ID mặc định (có thể là Anh-Việt)
    private static final String DEFAULT_SOURCE_ID = "en-vi";
    private String activeSourceId; // Theo dõi nguồn từ điển đang hoạt động


    public GeneralManagement() {
        sources = new HashMap<>();
        // Khởi tạo và thêm các nguồn từ điển cụ thể
        DictionarySource engVietSource = new EngVieManagement();
        sources.put(engVietSource.getSourceId(), engVietSource);

        DictionarySource vietEngSource = new VieEngManagement();
        sources.put(vietEngSource.getSourceId(), vietEngSource);

        // Đặt nguồn mặc định là active
        activeSourceId = DEFAULT_SOURCE_ID;
    }

    /**
     * Nạp dữ liệu cho TẤT CẢ các nguồn từ điển đã đăng ký.
     */
    public void loadAllSourcesData() {
        System.out.println("Đang nạp dữ liệu cho tất cả các nguồn từ điển...");
        for (DictionarySource source : sources.values()) {
            source.loadData(); // Gọi loadData() cho từng nguồn
        }
        System.out.println("Hoàn tất nạp dữ liệu.");
    }

    /**
     * Lưu dữ liệu cho TẤT CẢ các nguồn từ điển đã đăng ký.
     */
    public void saveAllSourcesData() {
        System.out.println("Đang lưu dữ liệu cho tất cả các nguồn từ điển...");
        for (DictionarySource source : sources.values()) {
            source.saveData(); // Gọi saveData() cho từng nguồn
        }
        System.out.println("Hoàn tất lưu dữ liệu.");
    }

    /**
     * Lấy nguồn từ điển đang hoạt động.
     * @return DictionarySource đang hoạt động.
     */
    public DictionarySource getActiveSource() {
        return sources.get(activeSourceId);
    }

    /**
     * Thiết lập nguồn từ điển hoạt động dựa trên SourceId.
     * @param sourceId SourceId của nguồn muốn kích hoạt.
     * @return true nếu sourceId hợp lệ và được thiết lập thành công, false nếu không.
     */
    public boolean setActiveSource(String sourceId) {
        if (sources.containsKey(sourceId)) {
            this.activeSourceId = sourceId;
            System.out.println("Đã chuyển nguồn từ điển sang: " + sources.get(sourceId).getDisplayName());
            return true;
        }
        System.err.println("SourceId '" + sourceId + "' không tồn tại.");
        return false;
    }

    /**
     * Lấy danh sách các SourceId có sẵn.
     */
    public Set<String> getAvailableSourceIds() {
        return sources.keySet();
    }

    /**
     * Lấy danh sách các tên hiển thị của các nguồn có sẵn.
     */
    public List<String> getAvailableSourceDisplayNames() {
        List<String> displayNames = new ArrayList<>();
        for (DictionarySource source : sources.values()) {
            displayNames.add(source.getDisplayName());
        }
        return displayNames;
    }

    /**
     * Lấy SourceId dựa trên tên hiển thị.
     * @param displayName Tên hiển thị.
     * @return SourceId tương ứng hoặc null nếu không tìm thấy.
     */
    public String getSourceIdByDisplayName(String displayName) {
        for (DictionarySource source : sources.values()) {
            if (source.getDisplayName().equals(displayName)) {
                return source.getSourceId();
            }
        }
        return null;
    }
}
