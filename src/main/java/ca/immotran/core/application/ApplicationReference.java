package ca.immotran.core.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Une reference personnelle ou professionnelle fournie avec une candidature. */
@Entity
@Table(name = "application_references")
public class ApplicationReference {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(length = 200)
    private String email;

    protected ApplicationReference() {
        // constructeur requis par JPA, ne pas utiliser directement
    }

    public ApplicationReference(Application application, String name, String phone, String email) {
        this.application = application;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
