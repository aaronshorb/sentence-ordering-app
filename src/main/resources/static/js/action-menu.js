document.addEventListener("click", (event) => {
    const openMenus = document.querySelectorAll(".action-menu[open]");

    if (openMenus.length === 0) {
        return;
    }

    const clickedMenu = event.target instanceof Element
        ? event.target.closest(".action-menu")
        : null;

    openMenus.forEach((menu) => {
        if (menu !== clickedMenu) {
            menu.removeAttribute("open");
        }
    });
});

document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") {
        return;
    }

    document.querySelectorAll(".action-menu[open]").forEach((menu) => {
        menu.removeAttribute("open");
    });
});
