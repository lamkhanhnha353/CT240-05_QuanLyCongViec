package com.teamwork.patterns;

public abstract class BaseReportGenerator {

   
    public final String generateReport(int projectId) {
        String rawData = fetchData(projectId); 
        String formattedData = formatData(rawData); 
        return addHeader() + formattedData + addFooter(); 
    }

    
    protected abstract String fetchData(int projectId);

    protected abstract String formatData(String rawData);

 
    private String addHeader() {
        return "=== BÁO CÁO DỰ ÁN TEAMWORK MASTER ===\n\n";
    }

    private String addFooter() {
        return "\n\n=== NGƯỜI XUẤT: HỆ THỐNG TỰ ĐỘNG ===";
    }
}