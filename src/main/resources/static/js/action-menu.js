document.addEventListener("click", (event) => {
    const openMenus = document.querySelectorAll(".action-menu[open]");
    const clickedMenu = event.target.closest(".action-menu");

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
