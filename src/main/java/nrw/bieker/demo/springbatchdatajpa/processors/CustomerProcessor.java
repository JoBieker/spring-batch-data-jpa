package nrw.bieker.demo.springbatchdatajpa.processors;

import nrw.bieker.demo.springbatchdatajpa.entities.Customer;
import nrw.bieker.demo.springbatchdatajpa.entities.InputItem;
import nrw.bieker.demo.springbatchdatajpa.entities.OutputItem;
import nrw.bieker.demo.springbatchdatajpa.repositories.customers.CustomerRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Optional;


public class CustomerProcessor implements ItemProcessor<InputItem, OutputItem> {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerProcessor(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
    }

    @Override
    public OutputItem process(InputItem item) throws Exception {
        OutputItem outputItem = new OutputItem();
        outputItem.setCompanyname(item.getCompanyname());
        Optional<Customer> optionalCustomer = customerRepository.findByCompanyName(item.getCompanyname());
        if(optionalCustomer.isPresent()){
            Customer customer = optionalCustomer.get();
            outputItem.setCustomerid(customer.getCustomerId());
        } else {
            System.out.println("Fuck!");
        }
        return outputItem;
    }
}
