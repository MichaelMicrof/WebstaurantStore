import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WebstaurantStoreTest {

    private static final String RETURN_MESSAGE = "Title contains word Table";
    private static final String URL_WEBSTAURANT_STORE = "https://www.webstaurantstore.com/";
    private static final String TEXT_FOR_SEARCH = "stainless work table\n";
    private static final String PAGE_LOCATOR_FOR_NUMBER = "//li[contains(@class, \"rc-pagination-item-";
    private static final String TITLE_CART_TEXT = "Cart";
    private static final String TEXT_RECYCLING_BIN_EMPTY = "Recycling bin is not empty";

    private static final By SEARCH_FIELD = By.id("searchval");
    private static final By LAST_PAGE =
            By.xpath("//li[contains(@class, \"rc-pagination-next\")]/preceding-sibling::li[1]");
    private static final By RESULT_OF_SEARCH = By.xpath("//a[@data-testid=\"itemDescription\"]");
    private static final By COUNT_ITEMS_IN_CART = By.id("cartItemCountSpan");
    private static final By LAST_BUTTON_ADD_TO_CART =
            By.xpath("(//input[@name=\"addToCartButton\"])[last()]");
    private static final By BUTTON_SUBMIT = By.xpath("//button[text()=\"Add To Cart\"]");
    private static final By CART_BUTTON = By.xpath("//span[text()=\"Cart\"]");
    private static final By TITLE_CART = By.tagName("h1");
    private static final By EMPTY_CART_BUTTON = By.className("emptyCartButton");
    private static final By ACCEPT_EMPTY =
            By.xpath("//div[@class=\"modal-footer\"]/button[text()=\"Empty Cart\"]");
    private static final By IMG_EMPTY_CART = By.xpath("//div[@class=\"image-container\"]/img");

    private WebDriver driver;
    private WebDriverWait wait;
    private final SoftAssert softAssert = new SoftAssert();

    private static WebDriver createDriver() {
        WebDriver driver;
        WebDriverManager.chromedriver().setup();

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--window-size=1920,1080");
        chromeOptions.setHeadless(true);

        driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().deleteAllCookies();

        return driver;
    }

    private WebDriverWait getWait() {
        if (wait == null) {
            wait = new WebDriverWait(driver, 10);
        }
        return wait;
    }

    private static String isRequest(List<WebElement> list, String str) {
        StringBuilder message = new StringBuilder();
        for (WebElement element : list) {
            if (!element.getText().contains(str)) {
                message.append("\n").append(element.getText());
            }
        }
        if (!message.toString().equals("")) return message.toString();
        return RETURN_MESSAGE;
    }

    private static void jsClick(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }

    private static void scroll(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView();", element);
    }

    private static void scrollClick(WebDriver driver, WebElement element) {
        scroll(driver, element);
        element.click();
    }

    @BeforeMethod
    private void setUp() {
        driver = createDriver();
    }

    @Test
    public void webstaurantStoreTest() {

        driver.get(URL_WEBSTAURANT_STORE);
        WebElement input = driver.findElement(SEARCH_FIELD);
        input.sendKeys(TEXT_FOR_SEARCH);

        WebElement lastPage = driver.findElement(LAST_PAGE);
        String xpathLocator;

        int lastPageNumber = Integer.parseInt(lastPage.getText());
        for (int i = 1; i <= lastPageNumber; i++) {
            xpathLocator = PAGE_LOCATOR_FOR_NUMBER.concat(String.valueOf(i)).concat("\")]");
            WebElement currentPage = driver.findElement(By.xpath(xpathLocator));
            getWait().until(ExpectedConditions.elementToBeClickable(By.xpath(xpathLocator)));
            currentPage.click();
            List<WebElement> resultSearch = driver.findElements(RESULT_OF_SEARCH);
            softAssert.assertEquals(isRequest(resultSearch, "Table"), RETURN_MESSAGE);
        }

        int numberInCartBefore =
                Integer.parseInt(driver.findElement(COUNT_ITEMS_IN_CART).getText());
        WebElement lastButtonAddToCart = driver.findElement(LAST_BUTTON_ADD_TO_CART);
        scrollClick(driver, lastButtonAddToCart);
        WebElement buttonSubmit = driver.switchTo().activeElement();
        buttonSubmit.findElement(BUTTON_SUBMIT).click();

        int numberInCartAfter =
                Integer.parseInt(driver.findElement(COUNT_ITEMS_IN_CART).getText());
        softAssert.assertEquals(numberInCartBefore + 1, numberInCartAfter);

        WebElement cartButton = driver.findElement(CART_BUTTON);
        jsClick(driver, cartButton);

        WebElement titleCart = driver.findElement(TITLE_CART);
        softAssert.assertEquals(titleCart.getText(), TITLE_CART_TEXT);

        WebElement emptyCartButton = driver.findElement(EMPTY_CART_BUTTON);
        emptyCartButton.click();
        WebElement acceptEmpty = driver.switchTo().activeElement();
        acceptEmpty.findElement(ACCEPT_EMPTY).click();

        WebElement imgEmptyCart = driver.findElement(IMG_EMPTY_CART);
        getWait().until(ExpectedConditions.visibilityOf(imgEmptyCart));
        softAssert.assertEquals(imgEmptyCart.isDisplayed(), true, TEXT_RECYCLING_BIN_EMPTY);
        softAssert.assertAll();
    }

    @AfterMethod
    private void tierDown() {
        driver.close();
        wait = null;
    }
}
