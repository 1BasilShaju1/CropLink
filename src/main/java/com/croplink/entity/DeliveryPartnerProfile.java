package com.croplink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_partner_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "service_area")
    private String serviceArea;

    private String status;
}
