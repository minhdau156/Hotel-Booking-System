package com.hotel.hotel_booking_system;

import com.hotel.hotel_booking_system.service.CurrentUserContext;
import com.hotel.hotel_booking_system.ui.login.LoginFrame;

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
        

        
        SwingUtilities.invokeLater(() -> {
            
            LoginFrame loginFrame = context.getBean(LoginFrame.class);
            CurrentUserContext userContext = context.getBean(CurrentUserContext.class);
            loginFrame.setVisible(true);
            
        });
	}

}
