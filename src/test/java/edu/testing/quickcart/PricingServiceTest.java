package edu.testing.quickcart;
import edu.testing.quickcart.model.*;
import edu.testing.quickcart.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class PricingServiceTest {

    private final PricingService service = new PricingService();

    private static final LocalDate COUPON_START = LocalDate.of(2026, 11, 1);
    private static final LocalDate COUPON_END = LocalDate.of(2026, 11, 30);

    // Decision Table R1-R12
    @ParameterizedTest(name = "{index}: {0} price={1} qty={2} tier={3} coupon={4} province={5} date={6} -> total={10}")
    @CsvSource({
        "R1,20.00,1,STANDARD,,ON,2026-11-15,20.00,8.99,3.77,32.76",
        "R2,120.00,1,STANDARD,10.00,ON,2026-12-15,120.00,0.00,15.60,135.60",
        "R3,5.00,12,STANDARD,10.00,ON,2026-10-15,57.00,8.99,8.58,74.57",
        "R4,20.00,1,GOLD,,ON,2026-11-15,18.00,8.99,3.51,30.50",
        "R5,10.00,12,GOLD,,ON,2026-11-15,102.60,0.00,13.34,115.94",
        "R6,40.00,1,STANDARD,10.00,ON,2026-11-15,40.00,8.99,6.37,55.36",
        "R7,55.00,1,GOLD,10.00,ON,2026-11-15,49.50,8.99,7.60,66.09",
        "R8,60.00,1,STANDARD,10.00,ON,2026-11-15,50.00,8.99,7.67,66.66",
        "R9,105.00,1,STANDARD,10.00,ON,2026-11-15,95.00,8.99,13.52,117.51",
        "R10,120.00,1,STANDARD,10.00,ON,2026-11-15,110.00,0.00,14.30,124.30",
        "R11,60.00,1,STANDARD,70.00,ON,2026-11-15,0.00,8.99,1.17,10.16",
        "R12,15.00,12,GOLD,10.00,ON,2026-11-15,143.90,0.00,18.71,162.61"
    })
    void decisionTableRule(String id, BigDecimal unitPrice, int qty, CustomerTier tier,
                           BigDecimal couponAmount, String province, LocalDate orderDate,
                           BigDecimal merch, BigDecimal shipping, BigDecimal tax, BigDecimal total) {
        Product p = new Product("P1", "Item", unitPrice);
        Coupon coupon = couponAmount == null ? null : new Coupon("SAVE", couponAmount, COUPON_START, COUPON_END);
        OrderQuote q = service.quote(List.of(new CartItem(p, qty)), tier, coupon, province, orderDate);
        assertAll(
            () -> assertEquals(merch, q.merchandise(), "merchandise"),
            () -> assertEquals(shipping, q.shipping(), "shipping"),
            () -> assertEquals(tax, q.tax(), "tax"),
            () -> assertEquals(total, q.total(), "total"));
    }

    // BCC T1-T6
    @ParameterizedTest(name = "{index}: {0} province={1} -> total={5}")
    @CsvSource({
        "T1,ON,20.00,8.99,3.77,32.76",
        "T2,BC,20.00,8.99,3.48,32.47",
        "T3,AB,20.00,8.99,1.45,30.44"
    })
    void bccProvince(String id, String province,
                     BigDecimal merch, BigDecimal shipping, BigDecimal tax, BigDecimal total) {
        Product p = new Product("P1", "Item", new BigDecimal("20.00"));
        OrderQuote q = service.quote(List.of(new CartItem(p, 1)), CustomerTier.STANDARD,
                null, province, LocalDate.of(2026, 11, 25));
        assertAll(
            () -> assertEquals(merch, q.merchandise(), "merchandise"),
            () -> assertEquals(shipping, q.shipping(), "shipping"),
            () -> assertEquals(tax, q.tax(), "tax"),
            () -> assertEquals(total, q.total(), "total"));
    }

    @Test void T4_unsupportedProvinceRejected() {
        Product p = new Product("P1", "Item", new BigDecimal("20.00"));
        assertThrows(IllegalArgumentException.class,
            () -> service.quote(List.of(new CartItem(p, 1)), CustomerTier.STANDARD,
                    null, "QC", LocalDate.of(2026, 11, 25)));
    }

    @Test void T5_emptyCartRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> service.quote(List.of(), CustomerTier.STANDARD,
                    null, "ON", LocalDate.of(2026, 11, 25)));
    }

    @Test void T6_multipleLines() {
        List<CartItem> items = List.of(
            new CartItem(new Product("P1", "Item", new BigDecimal("15.00")), 1),
            new CartItem(new Product("P2", "Item", new BigDecimal("5.00")), 1));
        OrderQuote q = service.quote(items, CustomerTier.STANDARD, null, "ON", LocalDate.of(2026, 11, 25));
        assertAll(
            () -> assertEquals(new BigDecimal("20.00"), q.merchandise(), "merchandise"),
            () -> assertEquals(new BigDecimal("8.99"), q.shipping(), "shipping"),
            () -> assertEquals(new BigDecimal("3.77"), q.tax(), "tax"),
            () -> assertEquals(new BigDecimal("32.76"), q.total(), "total"));
    }

    // BVT B1-B16
    @ParameterizedTest(name = "{index}: {0} price={1} qty={2} coupon={3} date={4} -> total={8}")
    @CsvSource({
        "B1,5.00,9,,2026-11-15,45.00,8.99,7.02,61.01",
        "B2,5.00,10,,2026-11-15,47.50,8.99,7.34,63.83",
        "B3,5.00,11,,2026-11-15,52.25,8.99,7.96,69.20",
        "B4,49.99,1,10.00,2026-11-15,49.99,8.99,7.67,66.65",
        "B5,50.00,1,10.00,2026-11-15,40.00,8.99,6.37,55.36",
        "B6,50.01,1,10.00,2026-11-15,40.01,8.99,6.37,55.37",
        "B7,60.00,1,59.99,2026-11-15,0.01,8.99,1.17,10.17",
        "B8,60.00,1,60.00,2026-11-15,0.00,8.99,1.17,10.16",
        "B9,60.00,1,60.01,2026-11-15,0.00,8.99,1.17,10.16",
        "B10,99.99,1,,2026-11-15,99.99,8.99,14.17,123.15",
        "B11,100.00,1,,2026-11-15,100.00,0.00,13.00,113.00",
        "B12,100.01,1,,2026-11-15,100.01,0.00,13.00,113.01",
        "B13,60.00,1,10.00,2026-10-31,60.00,8.99,8.97,77.96",
        "B14,60.00,1,10.00,2026-11-01,50.00,8.99,7.67,66.66",
        "B15,60.00,1,10.00,2026-11-30,50.00,8.99,7.67,66.66",
        "B16,60.00,1,10.00,2026-12-01,60.00,8.99,8.97,77.96"
    })
    void boundary(String id, BigDecimal unitPrice, int qty, BigDecimal couponAmount, LocalDate orderDate,
                  BigDecimal merch, BigDecimal shipping, BigDecimal tax, BigDecimal total) {
        Product p = new Product("P1", "Item", unitPrice);
        Coupon coupon = couponAmount == null ? null : new Coupon("SAVE", couponAmount, COUPON_START, COUPON_END);
        OrderQuote q = service.quote(List.of(new CartItem(p, qty)), CustomerTier.STANDARD,
                coupon, "ON", orderDate);
        assertAll(
            () -> assertEquals(merch, q.merchandise(), "merchandise"),
            () -> assertEquals(shipping, q.shipping(), "shipping"),
            () -> assertEquals(tax, q.tax(), "tax"),
            () -> assertEquals(total, q.total(), "total"));
    }
}
