-- Tables used by the native authentication and menu queries.
CREATE TABLE IF NOT EXISTS perfil (
    perfil_id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (perfil_id)
);

CREATE TABLE IF NOT EXISTS perfil_menu (
    perfil_id INT NOT NULL,
    menu_id INT NOT NULL,
    PRIMARY KEY (perfil_id, menu_id),
    FOREIGN KEY (perfil_id) REFERENCES perfil (perfil_id),
    FOREIGN KEY (menu_id) REFERENCES menu (menu_id)
);
