package me.martiandreamer.model;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.function.Consumer;

@ApplicationScoped
@Getter
public class Configuration {
    @ConfigProperty(name = "my-free-time.config.max-variant")
    Integer maxVariantInMinus;
    @ConfigProperty(name = "my-free-time.config.checkin")
    @Setter
    Boolean checkin;
    @ConfigProperty(name = "my-free-time.config.checkout")
    @Setter
    Boolean checkout;
    @ConfigProperty(name = "my-free-time.config.checkin-before-h")
    Integer checkinBeforeH;
    @ConfigProperty(name = "my-free-time.config.checkin-before-m")
    Integer checkinBeforeM;
    @ConfigProperty(name = "my-free-time.config.checkout-after-h")
    Integer checkoutAfterH;
    @ConfigProperty(name = "my-free-time.config.checkout-after-m")
    Integer checkoutAfterM;
    @ConfigProperty(name = "quarkus.http.port")
    String port;
    String email;
    @Setter
    private Boolean reCheckOut = true;

    public void setMaxVariantInMinus(int maxVariantInMinus) {
        if (maxVariantInMinus < 0) {
            throw new IllegalArgumentException("maxVariantInMinus cannot be negative");
        }
        this.maxVariantInMinus = maxVariantInMinus;
    }

    public void setCheckinBeforeH(int checkinBeforeH) {
        if (checkinBeforeH > 23 || checkinBeforeH < 0) {
            throw new IllegalArgumentException("hour must be between 0 and 23");
        }
        this.checkinBeforeH = checkinBeforeH;
    }

    public void setCheckinBeforeM(int checkinBeforeM) {
        if (checkinBeforeM > 59 || checkinBeforeM < 0) {
            throw new IllegalArgumentException("minute must be between 0 and 59");
        }
        this.checkinBeforeM = checkinBeforeM;
    }

    public void setCheckoutAfterH(int checkoutAfterH) {
        if (checkoutAfterH > 23 || checkoutAfterH < 0) {
            throw new IllegalArgumentException("hour must be between 0 and 23");
        }
        this.checkoutAfterH = checkoutAfterH;
    }

    public void setCheckoutAfterM(int checkoutAfterM) {
        if (checkoutAfterM > 59 || checkoutAfterM < 0) {
            throw new IllegalArgumentException("minute must be between 0 and 59");
        }
        this.checkoutAfterM = checkoutAfterM;
    }

    public void setEmail(String email) {
        if (email.matches(".+@.+[.].+")) {
            this.email = email;
        }
    }

    public Configuration update(Configuration configuration) {
        if (configuration != null) {
            checkAndSet(this::setMaxVariantInMinus, configuration.getMaxVariantInMinus());
            checkAndSet(this::setCheckinBeforeH, configuration.getCheckinBeforeH());
            checkAndSet(this::setCheckinBeforeM, configuration.getCheckinBeforeM());
            checkAndSet(this::setCheckoutAfterH, configuration.getCheckoutAfterH());
            checkAndSet(this::setCheckoutAfterM, configuration.getCheckoutAfterM());
            checkAndSet(this::setCheckin, configuration.getCheckin());
            checkAndSet(this::setCheckout, configuration.getCheckout());
            checkAndSet(this::setEmail, configuration.getEmail());
            checkAndSet(this::setReCheckOut, configuration.getReCheckOut());
        }
        return this;
    }

    private <T> void checkAndSet(Consumer<T> consumer, T object) {
        if (object != null) {
            consumer.accept(object);
        }
    }

    public int checkinBeforeMinuses() {
        return checkinBeforeH * 60 + checkinBeforeM;
    }

    public int checkoutAfterMinuses() {
        return checkoutAfterH * 60 + checkoutAfterM;
    }
}
