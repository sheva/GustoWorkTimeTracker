package com.essheva;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TimeTrackerExcelReader {

    final private Sheet sheet;

    TimeTrackerExcelReader(Path file) throws IOException, InvalidFormatException {
        Workbook workbook = new XSSFWorkbook(file.toFile());
        this.sheet = getLastSheet(workbook);
    }

    private Sheet getLastSheet(Workbook workbook) {
        return workbook.getSheetAt(workbook.getNumberOfSheets() - 1);
    }

    List<DayRecord> getRecords() {
        List<DayRecord> records = new ArrayList<>();
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            DayRecord record = new DayRecord();
            int j = 0;
            for (Cell cell: row) {
                switch (cell.getCellType()) {
                    case STRING:
                        record.setDescription(cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        String data_format = cell.getCellStyle().getDataFormatString();
                        if (data_format.equals("m/d/yy")) {
                            LocalDate date = cell.getDateCellValue().toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate();
                            record.setDate(date);
                        } else if (data_format.contains("h:mm:ss")) {
                            LocalTime time = Instant.ofEpochMilli(cell.getDateCellValue().getTime())
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalTime();
                            Row header = sheet.getRow(0);
                            String cellValue = header.getCell(j).getStringCellValue();
                            switch (cellValue) {
                                case "Start Work": record.setStart(time); break;
                                case "End Work": record.setEnd(time); break;
                                case "Start Break": record.setBreakStarted(time); break;
                            }
                        } else {
                            record.setBreakDuration((int)(cell.getNumericCellValue()));
                        }
                        break;
                    case BLANK:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + cell.getCellType());
                }
                j++;
            }
            System.out.println(record);
            records.add(record);
        }
        return records;
    }
}

@ToString
class DayRecord {
    @Setter @Getter private LocalDate date;
    @Setter @Getter private LocalTime start;
    @Setter @Getter private LocalTime end;
    @Setter @Getter private LocalTime breakStarted;
    @Setter @Getter private Integer breakDuration;
    @Setter @Getter private String description;
}
