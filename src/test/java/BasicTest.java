import junit.framework.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;

public class BasicTest extends TestHelper {


    private String username = "";
    private String password = "";

    //Test if items do get added to shopping cart
    @Test
    public void addAllItemsToShoppingCart() throws InterruptedException {

        List<WebElement> entries = getEntries();

        assertEquals(0, getCartEntries().size());

        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(2);
        List<WebElement> cartEntries = getCartEntries();
        assertEquals(entries.size(), cartEntries.size());
    }

    //Test if the items added to the shopping cart match in names and in order
    @Test
    public void testShoppingCartNamesMatchCatalogue() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(2);
        ArrayList<String> names = new ArrayList<String>();
        List<WebElement> cartEntries = getCartEntries();
        for (int i = 0; i < cartEntries.size(); i++) {
            String cartEntry = getShoppingCartItemTitle(cartEntries.get(i));
            names.add(cartEntry);
        }
        for (int i = 0; i < entries.size(); i++) {
            String shopEntry = getItemTitle(entries.get(i));
            if (!names.contains(shopEntry))
                assertEquals("Cart doesn't contain the item:", shopEntry, false);
        }
    }
    //Test if the items' prices added to the shopping cart match the prices in the catalogue
    @Test
    public void testShoppingCartPrices() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        HashMap<String, Double> prices = getAllPrices();
        ArrayList<WebElement> cartEntries = (ArrayList<WebElement>) getCartEntries();
        for (WebElement element : cartEntries) {
            Double expectedPrice = prices.get(getShoppingCartItemTitle(element));
            Double realPrice = Double.parseDouble(getShoppingCartItemPrice(element).substring(1).trim());
            assertEquals(expectedPrice, realPrice);
        }
    }

    //Test if increasing and decreasing amounts in shopping cart works properly
    @Test
    public void testShoppingCartIncreaseAndDecrease() throws InterruptedException {
        Integer increaseAmount = 4;
        increaseQuantity(increaseAmount);
        increaseAmount++;
        String increaseAmountString = increaseAmount.toString() + "×";
        String realAmount = getShoppingCartAmount();
        assertEquals(increaseAmountString, realAmount);
        decreaseQuantity(increaseAmount);
        String originalAmount = "1" + "×";
        realAmount = getShoppingCartAmount();
        assertEquals(originalAmount, realAmount);
    }

    //Check if deleting the items by using delete button works
    @Test
    public void deleteItemsOneByOne() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(getCartEntries().size(), entries.size());
        for (int i = 0; i < entries.size(); i++) {
            removeFirstItem();
        }
        assertEquals(0, getCartEntries().size());
    }
    //Check if using the "Empty cart" button works
    @Test
    public void emptyCart() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(getCartEntries().size(), entries.size());
        emptyShoppingCart();
        assertEquals(0, getCartEntries().size());
    }

    //Check if items are under the caregories they are supposed to
    @Test
    public void categoryTesting() throws InterruptedException {

        HashMap<String, ArrayList<String>> hashMap = getCategories();
        switchToSunglassesTab();
        TimeUnit.SECONDS.sleep(1);

        ArrayList<String> sunglassTitles = hashMap.get("Sunglasses");
        ArrayList<String> realTitles = getTitles();
        assertEquals(sunglassTitles, realTitles);


        switchToBooksTab();
        TimeUnit.SECONDS.sleep(1);

        ArrayList<String> booksTitles = hashMap.get("Books");
        realTitles = getTitles();
        Collections.reverse(realTitles);
        assertEquals(booksTitles, realTitles);


        switchToOtherTab();
        TimeUnit.SECONDS.sleep(1);

        ArrayList<String> otherTitles = hashMap.get("Other");
        realTitles = getTitles();
        Collections.reverse(realTitles);
        assertEquals(otherTitles, realTitles);
    }

    //Check if the search box works correctly
    @Test
    public void testSearching() throws InterruptedException {
        HashMap<String, String> titleSearches = titleSearches();
        ArrayList<String> titles = getTitles();
        WebElement searchBox = getSearchBox();

        for (String title : titles) {

            TimeUnit.SECONDS.sleep(1);

            searchBox.sendKeys(titleSearches.get(title));
            TimeUnit.SECONDS.sleep(3);

            ArrayList<String> results = getTitles();
            assertEquals("The search results did not contain the nessecary object", true, results.contains(title));
            searchBox.clear();
        }
    }
    //Check if buying singular items gives us the right price
    @Test
    public void testCheckoutPricesWithSingleQuantities() throws InterruptedException {
        List<WebElement> entries = getEntries();
        HashMap<String, Double> prices = getAllPrices();

        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        clickCheckoutButton();
        TimeUnit.SECONDS.sleep(1);
        fillDetailsForm("Peeter Meeter", "Peetri 1", "peeter.meeter@emeeter.ee", "Check");
        TimeUnit.SECONDS.sleep(2);
        clickPlaceOrderButton();
        TimeUnit.SECONDS.sleep(1);
        Double total = getTotal();
        assertEquals(prices.get("Total"), total);
    }

    //Check if buying items with greater quantity than 1 gives us the right price
    @Test
    public void testCheckoutPricesWithMultipleItems() throws InterruptedException {
        List<WebElement> entries = getEntries();
        HashMap<String, Double> prices = getAllPrices();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        String firstItem = getShoppingCartItemTitle(getCartEntries().get(0));
        HashMap<String, Double> allPrices = getAllPrices();
        System.out.println(firstItem);
        increaseQuantity(2);

        TimeUnit.SECONDS.sleep(1);
        clickCheckoutButton();
        TimeUnit.SECONDS.sleep(1);
        fillDetailsForm("Peeter Meeter", "Peetri 1", "peeter.meeter@emeeter.ee", "Check");
        TimeUnit.SECONDS.sleep(2);
        clickPlaceOrderButton();
        TimeUnit.SECONDS.sleep(1);
        Double total = getTotal();

        assertEquals(prices.get("Total") + allPrices.get(firstItem), total);
    }
}
