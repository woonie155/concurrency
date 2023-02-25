package com.example.concurrency.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Item {

    @Id @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private Long productId;

    private Long quantity;


    //
    public void decrease(Long quantity){
        if(this.quantity - quantity < 0) {
            throw new RuntimeException("lack of quantity");
        }
        this.quantity -= quantity;
    }
}
