package test;

import java201.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by jurijs.petrovs on 6/29/2017.
 */
@SpringBootTest(classes = {SpringBootApplication.class})
@ComponentScan(basePackageClasses = SpringBootApplication.class)
public class SpringTesting {
}
