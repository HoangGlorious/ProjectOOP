package com.example.btl;

public class Main {
    public static void main(String[] args) {
        Dictionary dictionary = new Dictionary();
        DictionaryManagement dictionaryManagement = new DictionaryManagement(dictionary);
        DictionaryCommandline dictionaryCommandline = new DictionaryCommandline(dictionary, dictionaryManagement);

        System.out.println("------- Chương trình Từ điển Anh-Việt -------");

        // Nạp dữ liệu từ resource classpath
        dictionaryManagement.insertFromFile();

        if (dictionary.getNumberOfEntries() > 0) {
            System.out.println("Từ điển đã được nạp ("+ dictionary.getNumberOfEntries() + " mục). Sẵn sàng tìm kiếm.");

            dictionaryCommandline.dictionaryAdvanced();

        } else {
            System.out.println("Nạp từ điển không thành công hoặc từ điển trống. Không thể tìm kiếm.");
        }

        // Đảm bảo đóng Scanner nếu bạn quản lý nó tập trung
        // Nếu chỉ chạy searcherInteractive một lần thì cần đóng sau đó
        if (!(dictionaryCommandline instanceof Object)) { // Kiểm tra để tránh lỗi nếu không có hàm closeScanner
            // dictionaryCommandline.closeScanner(); // Cân nhắc gọi ở đây nếu không dùng menu lặp
        }


        System.out.println("\nGoodbye, have a nice day ^^");
    }
}
