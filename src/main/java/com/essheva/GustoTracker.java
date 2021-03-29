package com.essheva;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;


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
        int offSet = 0;
        for (DayRecord record : records) {

            System.out.println("Processing record: " + record);

            int day = record.getDate().getDayOfMonth();
            String month = DateTimeFormatter.ofPattern("MMM").format(record.getDate());

            WebElement tr = null;
            while (tr == null) {
                js.executeScript("window.scrollBy(0,100)");
                tr = new WebDriverWait(driver, 5).until(
                        driver1 -> driver1.findElement(
                                By.xpath("//tr[td//span/text()='" + day + "' and td//span/text()='" + month + "']")));
            }


            try {
                tr.findElement(By.xpath(".//td[@data-title='Breakdown']//span[@class='breakdown__hours']"));
                offSet += 600;
                continue;
            } catch (NoSuchElementException ignore) {}

            js.executeScript("window.scrollTo(0, arguments[0])",  offSet);

            Actions actions = new Actions(driver);
            WebElement expand = tr.findElement(By.xpath(".//td[@data-title='Hours']"));
            actions.moveToElement(expand).perform();

            WebElement expandButton = tr.findElement(By.xpath(".//div[@class='time-log__result']//i"));
            while (!expandButton.isDisplayed()) {
                js.executeScript("window.scrollBy(0,300)");
            }
            actions.moveToElement(expandButton).click().perform();

            logData(js, record, tr);

            js.executeScript("window.scrollBy(0, 750)");

            WebElement submitButton = tr.findElement(By.xpath(".//div[@class='flex']//button"));
            submitButton.submit();

            js.executeScript("window.scrollBy(0, -800)");
            offSet += 600;
        }
    }

    private void logData(JavascriptExecutor js, DayRecord record, WebElement tr) {
        DateTimeFormatter gustoTimeFormatter = DateTimeFormatter.ofPattern("hh:mma");

        String startTime =  gustoTimeFormatter.format(record.getStart());
        String endTime =  gustoTimeFormatter.format(record.getEnd());
        String breakTime = gustoTimeFormatter.format(record.getBreakStarted());

        WebElement start = tr.findElement(By.xpath("(.//div[@class='time-log__shift']//input[@type='time'])[1]"));
        WebElement end = tr.findElement(By.xpath("(.//div[@class='time-log__shift']//input[@type='time'])[2]"));

        start.sendKeys(startTime);
        putData(endTime, end);
        WebElement breakRadioYes = tr.findElement(By.xpath(
                "(.//div[@class='break-input']//input[@type='radio'])[1]"));
        WebElement breakRadioNo = tr.findElement(By.xpath(
                "(.//div[@class='break-input']//input[@type='radio'])[2]"));
        js.executeScript("arguments[0].click();", breakRadioYes);

        WebElement breakStarted = tr.findElement(
                By.xpath(".//div[@class='break-input-secondary-group']//input[@name='break_start']"));
        breakStarted.sendKeys(breakTime);
        WebElement breakDuration = tr.findElement(
                By.xpath(".//div[@class='break-input-secondary-group']//input[@name='break_duration']"));
        breakDuration.clear();
        breakDuration.sendKeys(String.valueOf(record.getBreakDuration()));
        WebElement notes = tr.findElement(By.tagName("textarea"));
        notes.sendKeys(record.getDescription());
    }

    private void putData(String endTime, WebElement end) {
        end.sendKeys(endTime);
    }

    public static void main(String[] args) throws Exception {
        new GustoTracker();
    }
}

