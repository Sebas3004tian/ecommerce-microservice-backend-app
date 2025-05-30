package com.selimhorri.app.integration;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.repository.CartRepository;
import com.selimhorri.app.domain.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderServiceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto buildValidOrder(int cartId) {
        CartDto cart = CartDto.builder().cartId(cartId).userId(1).build();
        return OrderDto.builder()
            .orderId(1)
            .orderDate(LocalDateTime.now())
            .orderDesc("Test order")
            .orderFee(10.0)
            .cartDto(cart)
            .build();
    }

    @Test
    void testCreateOrder() throws Exception {
        Cart cart = Cart.builder().cartId(1).userId(1).build();
        cartRepository.save(cart);
        OrderDto orderDto = buildValidOrder(1);
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderDesc").value("Test order"));
    }
    @Test
    void testGetOrderById() throws Exception {
        Cart cart = Cart.builder().cartId(2).userId(1).build();
        cartRepository.save(cart);
        Order order = orderRepository.save(Order.builder().cart(cart).build());
        mockMvc.perform(get("/api/orders/" + order.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getOrderId()));
    }
    @Test
    void testGetNonExistentOrderById() throws Exception {
        mockMvc.perform(get("/api/orders/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void testListOrders() throws Exception {
        Cart cart = Cart.builder().cartId(3).userId(1).build();
        cartRepository.save(cart);
        orderRepository.save(Order.builder().cart(cart).build());
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").exists());
    }
    @Test
    void testDeleteOrder() throws Exception {
        Cart cart = Cart.builder().cartId(5).userId(1).build();
        cartRepository.save(cart);
        Order order = orderRepository.save(Order.builder().cart(cart).build());
        mockMvc.perform(delete("/api/orders/" + order.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
} 