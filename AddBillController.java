package com.greenmart.addBill;

import com.greenmart.order.OrderNotFoundException;
import com.greenmart.order.OrderService;
import com.greenmart.orderdetail.OrderDetailNotFoundException;
import com.greenmart.orderdetail.OrderDetailService;
import com.greenmart.cart.CartIsChangeException;
import com.greenmart.cart.CartNotFoundException;
import com.greenmart.cart.CartService;
import com.greenmart.common.entity.Cart;
import com.greenmart.customer.CustomerDetails;
import com.greenmart.customer.CustomerService;
import com.greenmart.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Controller
public class AddBillController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CartService cartService;
    @Autowired
    private AddBillService addBillService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @PostMapping("/cart/add-order")
//    @Transactional(isolation = Isolation.SERIALIZABLE)
//    public String addOrder(RedirectAttributes redirectAttributes,
//                           @AuthenticationPrincipal CustomerDetails customerDetails,
//                           HttpSession httpSession,
//                           @RequestParam(name = "delivery") String delivery
//                           ){
//        try{
//            String email = customerDetails.getUsername();
//            Long id_cus = customerService.getCustomerByEmail(email).getId();
//            List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
////            List<Cart> cartListOfCustomer = cartService.listCartOfIdCustomer(id_cus);
//            if (!cartService.checkChange(listCartOfId)){
//                throw new CartIsChangeException("Cart is change!");
//            }
//            addBillService.orderBill(id_cus,delivery);
//            redirectAttributes.addFlashAttribute("message","ORDER COMPLETE!");
//        } catch (OrderDetailNotFoundException | CartNotFoundException | CartIsChangeException e) {
//            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());
//        }
//        return "redirect:/cart";
//    }
    @PostMapping("/cart/add-order")
    public String addOrder(RedirectAttributes redirectAttributes,
                           @AuthenticationPrincipal CustomerDetails customerDetails,
                           HttpSession httpSession,
                           @RequestParam("city") String city,
                           @RequestParam("district") String district,
                           @RequestParam("ward") String ward,
                           @RequestParam("address") String address,
                           @RequestParam("phone") String phone){
        try{
            String delivery = address + " Phuong " + ward + " Quan " + district + city;
            String email = customerDetails.getUsername();
            Long id_cus = customerService.getCustomerByEmail(email).getId();
            List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
//            List<Cart> cartListOfCustomer = cartService.listCartOfIdCustomer(id_cus);
            if (!cartService.checkChange(listCartOfId)){
                throw new CartIsChangeException("Cart is change!");
            }
            addBillService.orderBill(id_cus,delivery,phone);
            redirectAttributes.addFlashAttribute("message","ORDER COMPLETE!");
        } catch (OrderDetailNotFoundException | CartNotFoundException | CartIsChangeException | OrderNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());
        }
        return "redirect:/cart";
    }
//@PostMapping("/cart/add-order")
//public String orderWithTimeout(@AuthenticationPrincipal CustomerDetails customerDetails,
//                               @RequestParam(name = "delivery") String delivery,
//                               Model model,
//                               RedirectAttributes redirectAttributes) {
//    try {
//        String email = customerDetails.getUsername();
//        Long id_cus = customerService.getCustomerByEmail(email).getId();
//
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            try {
//                addBillService.orderWithTimeout(id_cus, delivery);
//            } catch (OrderDetailNotFoundException e) {
//                String errorMessage = e.getMessage();
//                model.addAttribute("messageOrder", errorMessage);
//            }
//        });
//        redirectAttributes.addFlashAttribute("message","ORDER COMPLETE!");
//        future.get(2, TimeUnit.MINUTES);
//
//        return "redirect:/cart";
//    } catch (ExecutionException e) {
//        model.addAttribute("messageOrder", "Fails");
//        return "redirect:/cart";
//    } catch (TimeoutException e) {
//        model.addAttribute("messageOrder", "TimeOuts");
//        return "redirect:/cart";
//    } catch (InterruptedException e) {
//        model.addAttribute("messageOrder", "Interrupt");
//        return "redirect:/cart";
//    }
//}


//    @PostMapping("/cart/add-order")
//    public String addOrder(RedirectAttributes redirectAttributes,
//                           @AuthenticationPrincipal CustomerDetails customerDetails,
//                           HttpSession httpSession,
//                           @RequestParam(name = "delivery") String delivery) {
//        try {
//            String email = customerDetails.getUsername();
//            Long id_cus = customerService.getCustomerByEmail(email).getId();
//
//            Semaphore semaphore = getSemaphoreForCart(id_cus);
//            if (semaphore.tryAcquire(1, 60, TimeUnit.SECONDS)) {  // Thử giữ semaphore trong 5 giây
//                try {
//                    List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
//                    if (listCartOfId == null) {
//                        throw new CartNotFoundException("Cart not found!");
//                    }
//
//                    if (!cartService.checkChange(listCartOfId)) {
//                        throw new CartIsChangeException("Cart is change!");
//                    }
//
//                    addBillService.orderBill(id_cus, delivery);
//                    redirectAttributes.addFlashAttribute("message", "ORDER COMPLETE!");
//                } finally {
//                    semaphore.release();  // Giải phóng semaphore
//                }
//            } else {
//                throw new CartIsChangeException("Another user is currently processing the cart. Please try again later.");
//            }
//
//        } catch (OrderDetailNotFoundException | CartNotFoundException | CartIsChangeException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return "redirect:/cart";
////    }
//    // Lưu trữ các semaphore của người dùng
//    private static final Map<Long, Semaphore> cartSemaphores = new ConcurrentHashMap<>();
//
//    // Lấy hoặc tạo semaphore cho người dùng
//    private Semaphore getSemaphoreForCart(Long customerId) {
//        return cartSemaphores.computeIfAbsent(customerId, k -> new Semaphore(1));
//    }
public class AddressDTO {
    private String city;
    private String district;
    private String ward;
    private String address;

    public AddressDTO() {
    }

    public AddressDTO(String city, String district, String ward, String address) {
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
}
