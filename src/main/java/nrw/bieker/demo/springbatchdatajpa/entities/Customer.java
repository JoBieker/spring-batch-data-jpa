package nrw.bieker.demo.springbatchdatajpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="customers")
@Data
public class Customer {
    @Id
    @Column(name = "customerid")
    private String customerId;

    @Column(name = "companyname")
    private String companyName;
}
