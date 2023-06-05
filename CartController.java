package com.greenmart.cart;

//import com.greenmart.bill.OrderService;
import com.greenmart.common.entity.Cart;
import com.greenmart.customer.CustomerDetails;
import com.greenmart.customer.CustomerService;
import com.greenmart.orderdetail.OrderDetailNotFoundException;
import com.greenmart.product.ProductNotFoundException;
import com.greenmart.product.ProductService;
import com.greenmart.product.UpdateProductException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ProductService productService;

//    @Autowired
//    private OrderService billService;
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal CustomerDetails customerDetails,
                           HttpSession httpSession,
                           Model model,RedirectAttributes redirectAttributes) throws CartNotFoundException {
        try{
            String email = customerDetails.getUsername();
            Long id_cus = customerService.getCustomerByEmail(email).getId();
            List<Cart> listCartOfId = cartService.listCartOfIdCustomer(id_cus);
            int check = cartService.checkCartAble(listCartOfId);
            if (check==0)
            {
                model.addAttribute("error","The Product in Cart that be sold out is deleted");
                listCartOfId = cartService.listCartOfIdCustomer(id_cus);
            } else if (check==2)
            {
                model.addAttribute("extraMessage","Updated the purchasable quantity of the product");
                listCartOfId = cartService.listCartOfIdCustomer(id_cus);
            }
            BigDecimal total = cartService.total(id_cus);
            httpSession.setAttribute("listCartOfId",listCartOfId);
            model.addAttribute("listCartOfId",listCartOfId);
//            model.addAttribute("total",total);
        }
        catch(CartNotFoundException ex)
        {
            redirectAttributes.addFlashAttribute("error",ex.getMessage());
        }
        return "cart";
    }
    @GetMapping("/cart/delete/{id}")
    public String deleteCart(@PathVariable(name = "id") Long id,
                             @AuthenticationPrincipal CustomerDetails customerDetails,
                             RedirectAttributes redirectAttributes)
    {
//        Cart cart = cartService.findById(id);
//        if (cart == null){
//            redirectAttributes.addFlashAttribute("error","The product don't have in cart");
//        }
//        cartService.delete(id);
//        redirectAttributes.addFlashAttribute("message","The product deleted successfully");
        try{
            cartService.delete(id);
            redirectAttributes.addFlashAttribute("message","The product deleted successfully");
        }catch (CartNotFoundException ex)
        {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }
    @GetMapping("/cart/{id}/plus-1")
    public String plusCart(@PathVariable(name = "id") Long id,
                           @AuthenticationPrincipal CustomerDetails customerDetails,
                           RedirectAttributes redirectAttributes){
        try{
            Cart cart = cartService.findById(id);
            if (cart.getQuantity()<=cart.getProduct().getQuantity()){
            cartService.updateCartQuantity(cart.getId(),1);}
        }catch (CartNotFoundException | ProductNotFoundException | UpdateProductException ex){
            redirectAttributes.addFlashAttribute("error",ex.getMessage());
        }
        return "redirect:/cart";
    }
    @GetMapping("/cart/{id}/minus-1")
    public String minusCart(@PathVariable(name = "id") Long id,
                           @AuthenticationPrincipal CustomerDetails customerDetails,
                            RedirectAttributes redirectAttributes){
//        Cart cart = cartService.findById(id);
//        if (cart.getQuantity()==1) cartService.delete(id);
//        else cartService.updateQuantity(cart,-1);
        try{
            Cart cart = cartService.findById(id);
            if (cart.getQuantity()<=cart.getProduct().getQuantity()){
            cartService.updateCartQuantity(cart.getId(),-1);}
        }catch (CartNotFoundException | ProductNotFoundException | UpdateProductException ex){
            redirectAttributes.addFlashAttribute("error",ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update-quantity")
    public String updateQuantity(@RequestParam(name = "quantity") List<Integer> quantity,
                                 @AuthenticationPrincipal CustomerDetails customerDetails,
                                 RedirectAttributes redirectAttributes) {
        try{
            String email = customerDetails.getUsername();
            Long id_cus = customerService.getCustomerByEmail(email).getId();
//            for (Integer integer :quantity){
//                System.out.println(integer);
//            }
            List<Cart> cartList = cartService.listCartCustomer(id_cus);
//            for (Cart cart :cartList){
////                System.out.println(cart.getId());
//            }
            cartService.updateListCart(cartList,quantity);
//            cartList = cartService.listCartOfIdCustomer(id_cus);
            if(cartService.checkCartAble(cartList)==1) {
                redirectAttributes.addFlashAttribute("message", "The quantity has been updated successfully");
            }
        }catch (CartNotFoundException ex)
        {
            redirectAttributes.addFlashAttribute("error",ex.getMessage());
        } catch (ProductNotFoundException e) {
            redirectAttributes.addFlashAttribute("error","Product is Sold Out, be deleted out my Cart.");
        } catch (UpdateProductException e) {
            redirectAttributes.addFlashAttribute("error","Product quantity is not enough, be updated biggest quantity.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",e.getMessage());
        }
        return "redirect:/cart";
    }
//    @GetMapping("/cart/confirm")
//    public String cartConfirm(@AuthenticationPrincipal CustomerDetails customerDetails,
//                              HttpSession httpSession,
//                              Model model,RedirectAttributes redirectAttributes) throws CartNotFoundException {
//        try{
//            String email = customerDetails.getUsername();
//            Long id_cus = customerService.getCustomerByEmail(email).getId();
//            List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
////            for (Cart cart : listCartOfId){
////                System.out.println(cart.getProduct().getId());
////                System.out.println(cart.getQuantity());
////            }
////            List<Cart> listCartOfIdInDB = cartService.listCartOfIdCustomer(id_cs);
////            System.out.println(cartService.checkChange(listCartOfId));
//            if (!cartService.checkChange(listCartOfId)){
//                throw new CartIsChangeException("Cart is change");
//            }
//            String delivery = customerService.getCustomerByEmail(email).getAddress();
//            BigDecimal total = cartService.total(id_cus);
//            httpSession.setAttribute("listCartOfId",listCartOfId);
//            model.addAttribute("listCartOfId",listCartOfId);
//            model.addAttribute("total", total);
//            model.addAttribute("delivery",delivery);
//        } catch(CartNotFoundException | CartIsChangeException ex){
//            redirectAttributes.addFlashAttribute("errorMessage",ex.getMessage());
//            return "redirect:/cart";
//        }
//        return "confirmOrder";
//    }
//    @GetMapping("/cart/confirm")
//    @Transactional(isolation = Isolation.SERIALIZABLE)
//    public String cartConfirm(@AuthenticationPrincipal CustomerDetails customerDetails,
//                              HttpSession httpSession,
//                              Model model,RedirectAttributes redirectAttributes) throws CartNotFoundException {
//        try{
//            String email = customerDetails.getUsername();
//            Long id_cus = customerService.getCustomerByEmail(email).getId();
//            Semaphore semaphore = getSemaphoreForCart(id_cus);  // Lấy semaphore cho người dùng
//
//            if (semaphore.tryAcquire(1, 60, TimeUnit.SECONDS)) {  // Thử giữ semaphore trong 5 giây
//                try {
//                    List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
////            for (Cart cart : listCartOfId){
////                System.out.println(cart.getProduct().getId());
////                System.out.println(cart.getQuantity());
////            }
//                    List<Cart> listCartOfIdInDB = cartService.listCartOfIdCustomer(id_cus);
////            System.out.println(cartService.checkChange(listCartOfId));
//                    if (!cartService.checkChange(listCartOfId)) {
//                        throw new CartIsChangeException("Cart is change");
//                    }
//                    String delivery = customerService.getCustomerByEmail(email).getAddress();
//                    BigDecimal total = cartService.total(id_cus);
//                    httpSession.setAttribute("listCartOfId", listCartOfId);
//                    model.addAttribute("listCartOfId", listCartOfId);
//                    model.addAttribute("total", total);
//                    model.addAttribute("delivery", delivery);
//                } finally {
//                    semaphore.release();  // Giải phóng semaphore
//                }
//            } else {
//                throw new CartIsChangeException("Another user is currently processing the cart. Please try again later.");
//            }
//        } catch (CartNotFoundException | CartIsChangeException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return "confirmOrder";
//    }
    @GetMapping("/cart/confirm")
    public String viewConfirm(@AuthenticationPrincipal CustomerDetails customerDetails,
                              HttpSession httpSession,
                              Model model, RedirectAttributes redirectAttributes) {

        try {
            String email = customerDetails.getUsername();
            Long id_cus = customerService.getCustomerByEmail(email).getId();

                List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
                if (listCartOfId == null) {
                    listCartOfId = cartService.listCartOfIdCustomer(id_cus);
                    httpSession.setAttribute("listCartOfId", listCartOfId);
                }

                if (!cartService.checkChange(listCartOfId)) {
                    throw new CartIsChangeException("Cart is change");
                }

                LocalDate date = LocalDate.now();
                String delivery = customerService.getCustomerByEmail(email).getAddress();
                BigDecimal total = cartService.total(id_cus);

                if (cartService.checkCartAble(listCartOfId) == 1) {
                    model.addAttribute("total", total);
                    model.addAttribute("delivery", delivery);
                } else if (cartService.checkCartAble(listCartOfId) == 2) {
                    model.addAttribute("message", "Updated the purchasable quantity of the product");
                    listCartOfId = cartService.listCartOfIdCustomer(id_cus);
                }

                model.addAttribute("check", cartService.checkCartAble(listCartOfId));
                model.addAttribute("listCartOfId", listCartOfId);
                model.addAttribute("date", date);
                model.addAttribute("name",listCartOfId.get(0).getCustomer().getFullName());
                model.addAttribute("phone",listCartOfId.get(0).getCustomer().getPhone());

        } catch (CartNotFoundException | CartIsChangeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }
        return "payment";
    }


//    @Transactional(isolation = Isolation.SERIALIZABLE)
//    @GetMapping("/cart/confirm-order")
//    public String viewConfirmOrder(@AuthenticationPrincipal CustomerDetails customerDetails,
//                              HttpSession httpSession,
//                              Model model,RedirectAttributes redirectAttributes){
//
//        try{
//            String email = customerDetails.getUsername();
//            Long id_cus = customerService.getCustomerByEmail(email).getId();
////            List<Cart> listCartOfId = (List<Cart>) httpSession.getAttribute("listCartOfId");
////            for (Cart cart : listCartOfId){
////                System.out.println(cart.getProduct().getId());
////                System.out.println(cart.getQuantity());
////            }
//            List<Cart> listCartOfId= cartService.listCartOfIdCustomer(id_cus);
////            System.out.println(cartService.checkChange(listCartOfId));
//            if (!cartService.checkChange(listCartOfId)){
//                throw new CartIsChangeException("Cart is change");
//            }
//            LocalDate date = LocalDate.now();
//            String delivery = customerService.getCustomerByEmail(email).getAddress();
//            BigDecimal total = cartService.total(id_cus);
//            if (cartService.checkCartAble(listCartOfId)==1) {
//                model.addAttribute("total", total);
//                model.addAttribute("delivery", delivery);
//            }
//            else if (cartService.checkCartAble(listCartOfId)==2){
//                model.addAttribute("message","Updated the purchasable quantity of the product");
//                listCartOfId=cartService.listCartOfIdCustomer(id_cus);
//            }
//                model.addAttribute("check",cartService.checkCartAble(listCartOfId));
//                httpSession.setAttribute("listCartOfId", listCartOfId);
//                model.addAttribute("listCartOfId", listCartOfId);
//                model.addAttribute("date", date);
//        } catch(CartNotFoundException | CartIsChangeException ex){
//            redirectAttributes.addFlashAttribute("errorMessage",ex.getMessage());
//            return "redirect:/cart";
//        }
//        return "confirm_order";
//    }

//    @PostMapping("/cart/add-order")
//    public String addOrder(RedirectAttributes redirectAttributes,
//                           @AuthenticationPrincipal CustomerDetails customerDetails,
//                           @RequestParam(name = "delivery") String delivery,
//                           @ModelAttribute(name = "listCartOfId") ArrayList<Cart> listCartOfId){
//        String email = customerDetails.getUsername();
//        Customer customer = customerService.getCustomerByEmail(email);
//        billService.save(customer.getId(),delivery);
////        cartService.deleteList(listCartOfId);
//        redirectAttributes.addFlashAttribute("message","ORDER COMPLETE!");
//        return "redirect:/cart";
//    }
}
