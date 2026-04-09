package com.beta.FindHome.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ErrorLogUtils {

    private static final int USERNAME_COL = 0;
    private static final int IP_ADDRESS_COL = 1;
    private static final int API_PATH_COL = 2;
    private static final int ERROR_MESSAGE_COL = 3;
    private static final int TIMESTAMP_COL = 4;

    public static String BASE_LOG_PATH = "logs/error/";

    public static void printLog(
            String userName,
            String ipAddress,
            String apiPath,
            String errorMessage
    ) throws IOException {

        LocalDate currentDate = LocalDate.now();
        String year = String.valueOf(currentDate.getYear());
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String directoryPath = BASE_LOG_PATH + year;
        Path logDirectory = Path.of(directoryPath);

        // Create directory if it does not exist
        if (!Files.exists(logDirectory)) {
            Files.createDirectories(logDirectory);
        }

        String filePath = directoryPath + "/" + formattedDate + ".xlsx";
        File file = new File(filePath);

        // Open or create the workbook
        try (XSSFWorkbook workbook = file.exists()
                ?
//                means if file exists, open it
                new XSSFWorkbook(new FileInputStream(file))
                :
//                else create a new workbook
                new XSSFWorkbook()) {

            // Check if there are any sheets and get the first sheet or create a new one
            XSSFSheet sheet;

//            check if there are any sheets
            if (workbook.getNumberOfSheets() > 0) {
//                means we have a sheet, so get the first sheet
                sheet = workbook.getSheetAt(0);
            } else {
//                else create a new sheet
                sheet = workbook.createSheet("Exception Logs");
            }

            // Create header row if new sheet
            if (sheet.getPhysicalNumberOfRows() == 0) {
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(USERNAME_COL).setCellValue("Username");
                headerRow.createCell(IP_ADDRESS_COL).setCellValue("IP Address");
                headerRow.createCell(API_PATH_COL).setCellValue("API Path");
                headerRow.createCell(ERROR_MESSAGE_COL).setCellValue("Error Message");
                headerRow.createCell(TIMESTAMP_COL).setCellValue("Timestamp");
            }

            // Append new data
            int rowNum = sheet.getPhysicalNumberOfRows();
            Row dataRow = sheet.createRow(rowNum);
            dataRow.createCell(USERNAME_COL).setCellValue(userName);
            dataRow.createCell(IP_ADDRESS_COL).setCellValue(ipAddress);
            dataRow.createCell(API_PATH_COL).setCellValue(apiPath);
            dataRow.createCell(ERROR_MESSAGE_COL).setCellValue(errorMessage);
            dataRow.createCell(TIMESTAMP_COL).setCellValue(LocalDate.now().toString());

            // Save to the file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }
}
