/* Confirmation dialog built on the mesnos design system dialog styles
 * (native <dialog class="dialog">, styled by css/vendor/mesnos.style.css). */

export function confirmDialog({ title, message, confirmLabel = "OK", cancelLabel = "Cancel", danger = false }) {
    return new Promise((resolve) => {
        const dialog = document.createElement("dialog");
        dialog.className = "dialog";

        const header = document.createElement("div");
        header.className = "dialog-header";
        const titleElt = document.createElement("span");
        titleElt.className = "dialog-title";
        titleElt.textContent = title;
        const close = document.createElement("button");
        close.className = "dialog-close";
        close.setAttribute("aria-label", "Close");
        close.textContent = "✕";
        header.append(titleElt, close);

        const body = document.createElement("div");
        body.className = "dialog-body";
        body.textContent = message;

        const footer = document.createElement("div");
        footer.className = "dialog-footer";
        const cancel = document.createElement("button");
        cancel.className = "btn secondary";
        cancel.textContent = cancelLabel;
        const confirm = document.createElement("button");
        confirm.className = danger ? "btn danger" : "btn btn-primary";
        confirm.textContent = confirmLabel;
        footer.append(cancel, confirm);

        dialog.append(header, body, footer);

        confirm.addEventListener("click", () => dialog.close("confirm"));
        cancel.addEventListener("click", () => dialog.close("cancel"));
        close.addEventListener("click", () => dialog.close("cancel"));
        dialog.addEventListener("close", () => {
            resolve(dialog.returnValue === "confirm");
            dialog.remove();
        });

        document.body.appendChild(dialog);
        dialog.showModal();
        cancel.focus();
    });
}

/* Fixed error banner, shared by the pages that call the API. */
export function showErrorMessage(message) {
    let box = document.getElementById("app-error");
    if (!box) {
        box = document.createElement("div");
        box.id = "app-error";
        box.className = "message message-danger";
        box.role = "alert";
        box.style.position = "fixed";
        box.style.top = "var(--space-2)";
        box.style.left = "var(--space-2)";
        box.style.right = "var(--space-2)";
        box.style.zIndex = "var(--z-modal, 1000)";
        document.body.appendChild(box);
    }
    box.textContent = message;
}
