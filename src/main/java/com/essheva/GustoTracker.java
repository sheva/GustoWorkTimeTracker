package com.essheva;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class GustoTracker {

    GustoTracker() throws IOException, InvalidFormatException {
        Configuration config = new Configuration();
        TimeTrackerExcelReader reader = new TimeTrackerExcelReader(config.getStatusFilePath());
        List<DayRecord> records = reader.getRecords();

        System.setProperty("webdriver.chrome.driver", config.getDriverPath().toAbsolutePath().toString());
        WebDriver driver = new ChromeDriver();

        driver.get("http://gusto.com/login");

        driver.findElement(By.id("user_email")).sendKeys(config.getUserEmail());
        driver.findElement(By.id("user_password")).sendKeys(config.getUserPassword());
        driver.findElement(By.id("btn-login")).submit();

        driver.get("https://app.gusto.com/time_tracking");

        logTime(records, driver);
    }

    private void logTime(List<DayRecord> records, WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int offset = 0;
        for (DayRecord record: records) {

            System.out.println("Processing record: " + record);

            int day = record.getDate().getDayOfMonth();
            String month = DateTimeFormatter.ofPattern("MMM").format(record.getDate());

            driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);

            WebElement tr = null;
            while (tr == null) {
                try {
                    js.executeScript("window.scrollBy(0,300)");
                    tr = driver.findElement(
                            By.xpath("//tr[td//span/text()='" + day + "' and td//span/text()='" + month + "']"));
                } catch(NoSuchElementException ignore) {}
            }

            try {
                tr.findElement(By.xpath(".//td[@data-title='Breakdown']//span[@class='breakdown__hours']"));
                offset += 500;
                continue;
            } catch (NoSuchElementException ignore) {}

            js.executeScript("window.scrollBy(0, arguments[0])",  offset);

            Actions actions = new Actions(driver);
            WebElement expand = tr.findElement(By.xpath(".//td[@data-title='Hours']"));
            actions.moveToElement(expand).perform();

            WebElement expandButton = tr.findElement(By.xpath(".//div[@class='time-log__result']//i"));
            while (!expandButton.isDisplayed()) {
                js.executeScript("window.scrollBy(0,200)");
            }
            actions.moveToElement(expandButton).click().perform();

            logData(js, record, tr);

            WebElement submitButton = tr.findElement(By.xpath(".//div[@class='flex']//button"));
            submitButton.submit();

            js.executeScript("window.scrollBy(0, -800)");
            offset += 600;
        }
    }

    private void logData(JavascriptExecutor js, DayRecord record, WebElement tr) {
        DateTimeFormatter gustoTimeFormatter = DateTimeFormatter.ofPattern("hh:mma");

        String startTime =  gustoTimeFormatter.format(record.getStart());
        String endTime =  gustoTimeFormatter.format(record.getEnd());

        WebElement start = tr.findElement(By.xpath("(.//div[@class='time-log__shift']//input[@type='time'])[1]"));
        WebElement end = tr.findElement(By.xpath("(.//div[@class='time-log__shift']//input[@type='time'])[2]"));

        start.sendKeys(startTime);
        end.sendKeys(endTime);

        if (record.getBreakDuration() != null) {
            String breakTime = gustoTimeFormatter.format(record.getBreakStarted());
            WebElement breakRadioYes = tr.findElement(By.xpath(
                    "(.//div[@class='break-input']//input[@type='radio'])[1]"));
            js.executeScript("arguments[0].click();", breakRadioYes);

            WebElement breakStarted = tr.findElement(
                    By.xpath(".//div[@class='break-input-secondary-group']//input[@name='break_start']"));
            breakStarted.sendKeys(breakTime);
            WebElement breakDuration = tr.findElement(
                    By.xpath(".//div[@class='break-input-secondary-group']//input[@name='break_duration']"));
            breakDuration.clear();
            breakDuration.sendKeys(String.valueOf(record.getBreakDuration()));
        }

        WebElement notes = tr.findElement(By.tagName("textarea"));
        notes.sendKeys(record.getDescription());
    }

    public static void main(String[] args) throws Exception {
        new GustoTracker();
    }
}

