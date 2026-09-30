package com.hotel.hotel_booking_system;

import javax.swing.SwingUtilities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class HotelBookingSystemApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context =
                new SpringApplicationBuilder(HotelBookingSystemApplication.class)
                .headless(false)
                .run(args);
        System.out.println(new BCryptPasswordEncoder().encode("12345678"));

        
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = context.getBean(MainFrame.class);
            frame.setVisible(true);
        });
	}

}
