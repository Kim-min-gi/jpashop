package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnougStockException;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
class OrderServiceTest {


    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{

        Member member = getMember();


        Book book = getBook();

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        Order getOrder = orderRepository.findOne(orderId);

        assertEquals( OrderStatus.ORDER,getOrder.getStatus());
        assertEquals(1,getOrder.getOrderItemList().size());
        assertEquals(1000 * orderCount,getOrder.getTotalPrice());
        assertEquals(8,book.getStockQuantity());

    }

    private Book getBook() {
        Book book = new Book();
        book.setName("시골 jpa");
        book.setPrice(1000);
        book.setStockQuantity(10);
        em.persist(book);
        return book;
    }

    private Member getMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","강가","123-123"));
        em.persist(member);
        return member;
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{
        Member member = getMember();
        Item item = getBook();

        int orderCount = 11;

        assertThrows(NotEnougStockException.class,() ->
                orderService.order(member.getId(), item.getId(), orderCount));

    }

    @Test
    public void 주문취소() throws Exception{

        Member member = getMember();
        Item item = getBook();

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(),item.getId(),orderCount);

        orderService.cancleOrder(orderId);

        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL,getOrder.getStatus());
        assertEquals(10,item.getStockQuantity());

    }


}