import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestHelper {

    static WebDriver driver;
    final int waitForResposeTime = 4;

    // here write a link to your admin website (e.g. http://my-app.herokuapp.com/admin)
    String baseUrlAdmin = "https://radiant-cove-46717.herokuapp.com/admin";

    // here write a link to your website (e.g. http://my-app.herokuapp.com/)
    String baseUrl = "https://radiant-cove-46717.herokuapp.com/";

    @Before
    public void setUp() {

        // if you use Chrome:
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Kristjan\\Documents\\chromedriver.exe");
        driver = new ChromeDriver();

        // if you use Firefox:
        //System.setProperty("webdriver.geckoins:maven-compiler-plugin:3.1:testCompile (default-testCompile) on project WebApplicationTestAutomation: Compilation failure: Compilation failure:
        //driver = new FirefoxDriver();

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(baseUrl);

    }

    void goToPage(String page) {
        WebElement elem = driver.findElement(By.linkText(page));
        elem.click();
        waitForElementById(page);
    }

    void waitForElementById(String id) {
        new WebDriverWait(driver, waitForResposeTime).until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
    }

    void waitForElementByXpath(String xPath) {
        new WebDriverWait(driver, waitForResposeTime).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xPath)));
    }

    void waitForElementByClass(String Class) {
        new WebDriverWait(driver, waitForResposeTime).until(ExpectedConditions.presenceOfElementLocated(new By.ByClassName(Class)));
    }

    public boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    void login(String username, String password) {

        driver.get(baseUrlAdmin);

        driver.findElement(By.linkText("Login")).click();

        driver.findElement(By.id("name")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value='Login']")).click();
    }

    List<WebElement> getEntries() {
        By by = By.className("entry");
        List<WebElement> entries = driver.findElements(by);
        return entries;
    }

    ArrayList<String> getTitles() {
        ArrayList<WebElement> sunglassItems = (ArrayList<WebElement>) getEntries();
        ArrayList<String> realTitles = new ArrayList<>();
        for (WebElement sunglassItem : sunglassItems) {
            realTitles.add(getItemTitle(sunglassItem));
        }
        return realTitles;
    }


    HashMap<String, String> titleSearches() {
        ArrayList<String> titles = getTitles();
        HashMap<String, String> searches = new HashMap<>();
        for (String title : titles) {
            if (title.length() > 5) {
                searches.put(title, title.substring(0, 5));
            } else {
                searches.put(title, title);
            }
        }
        return searches;
    }

    List<WebElement> getCartEntries() {
        waitForElementById("cart_row");
        return driver.findElements(By.className("cart_row"));
    }

    void addItemsToShoppingCart(List<WebElement> entries) {
        for (WebElement element :
                entries) {
            element.findElement(new By.ByXPath(".//div[2]/form/input[1]")).click();
        }
    }

    void addItemToShoppingCart(WebElement entry) {
        entry.findElement(new By.ByXPath(".//div[2]/form/input[1]")).click();
    }

    String getItemTitle(WebElement item) {
        return item.findElement(new By.ByXPath(".//h3//a")).getText();
    }

    String getShoppingCartItemTitle(WebElement item) {
        return item.findElement(new By.ByXPath(".//td[2]")).getText();
    }

    String getItemPrice(WebElement item) {
        return item.findElement(new By.ByClassName("price")).getText();
    }

    String getShoppingCartItemPrice(WebElement item) {
        return item.findElement(new By.ByClassName("item_price")).getText();
    }

    HashMap<String, Double> getAllPrices() {
        List<WebElement> elements = getEntries();
        Double total = 0.0;
        HashMap<String, Double> values = new HashMap<>();
        for (WebElement element : elements) {
            String name = getItemTitle(element);
            String priceString = getItemPrice(element);
            Double price = Double.parseDouble(priceString.substring(1));
            values.put(name, price);
            total += price;
        }
        values.put("Total", total);
        return values;
    }

    Double getTotal() {
        String totalString = driver.findElement(new By.ByXPath("//*[@id=\"check_out\"]/tbody/tr[5]/td[2]/strong")).getText();
        return Double.parseDouble(totalString.substring(1));
    }

    ArrayList<String> getShoppingCartTitles() {
        ArrayList<WebElement> cart = (ArrayList<WebElement>) getCartEntries();
        ArrayList<String> returnable = new ArrayList<>();
        for (WebElement element : cart) {
            returnable.add(getShoppingCartItemTitle(element));
        }
        return returnable;
    }

    void increaseQuantity(Integer increaseAmount) throws InterruptedException {
        addItemToShoppingCart(getEntries().get(0));
        for (int i = 0; i < increaseAmount; i++) {
            TimeUnit.SECONDS.sleep(1);
            List<WebElement> cartEntries = getCartEntries();
            WebElement firstEntry = cartEntries.get(0);
            WebElement increaseButton = firstEntry.findElement(new By.ByXPath(".//td[5]"));
            increaseButton.click();
        }
        TimeUnit.SECONDS.sleep(2);
    }

    void decreaseQuantity(Integer decreaseAmount) throws InterruptedException {
        for (int i = 0; i < decreaseAmount - 1; i++) {
            TimeUnit.SECONDS.sleep(1);
            List<WebElement> cartEntries = getCartEntries();
            WebElement firstEntry = cartEntries.get(0);
            WebElement decreaseButton = firstEntry.findElement(new By.ByXPath(".//td[4]"));
            decreaseButton.click();
        }
        TimeUnit.SECONDS.sleep(2);
    }

    public void removeFirstItem() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        List<WebElement> cartEntries = getCartEntries();
        WebElement firstEntry = cartEntries.get(0);
        WebElement decreaseButton = firstEntry.findElement(new By.ByXPath(".//td[6]//a"));
        decreaseButton.click();
    }

    String getShoppingCartAmount() {
        return driver.findElement(new By.ByXPath("//*[@id=\"cart\"]/table/tbody/tr[1]/td[1]")).getText();
    }

    WebElement getSearchBox() {
        return driver.findElement(new By.ById("search_input"));
    }

    void emptyShoppingCart() {
        driver.findElement(new By.ByXPath("//*[@id=\"cart\"]/form[1]/input[2]")).click();
    }

    HashMap<String, ArrayList<String>> getCategories() {
        HashMap<String, ArrayList<String>> returnable = new HashMap<String, ArrayList<String>>();
        returnable.put("Books", new ArrayList<>());
        returnable.put("Sunglasses", new ArrayList<>());
        returnable.put("Other", new ArrayList<>());
        List<WebElement> elements = getEntries();
        for (WebElement element : elements) {
            String title = getItemTitle(element);
            String category = element.findElement(new By.ById("category")).getText().substring(9).trim();
            if (!category.equals("Books") && !category.equals("Sunglasses")) {
                category = "Other";
            }
            returnable.get(category).add(title);
        }
        return returnable;
    }

    void clickCheckoutButton() {
        driver.findElement(new By.ByXPath("//*[@id=\"checkout_button\"]/input")).click();
    }

    void switchToBooksTab() {
        driver.findElement(new By.ByXPath("//body//div[2]//div[1]//ul//li[3]//a")).click();
    }

    void switchToSunglassesTab() {
        driver.findElement(new By.ByXPath("//body//div[2]//div[1]//ul//li[2]//a")).click();
    }

    void switchToOtherTab() {
        driver.findElement(new By.ByXPath("//body//div[2]//div[1]//ul//li[4]//a")).click();
    }

    void fillDetailsForm(String name, String address, String email, String payType) throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        WebElement nameField = driver.findElement(By.id("order_name"));
        nameField.sendKeys(name);
        WebElement addressField = driver.findElement(By.id("order_address"));
        addressField.sendKeys(address);
        WebElement emailField = driver.findElement(By.id("order_email"));
        emailField.sendKeys(email);
        Select paymentField = new Select(driver.findElement(By.id("order_pay_type")));
        paymentField.selectByValue(payType);
    }

    void clickPlaceOrderButton() {
        driver.findElement(new By.ByXPath("//*[@id=\"place_order\"]/input")).click();
    }

    void logout() {
        WebElement logout = driver.findElement(By.linkText("Logout"));
        logout.click();

        waitForElementById("Admin");
    }

    @After
    public void tearDown() {
        driver.close();
    }

}