/* Confirmations use the shared dialog of the mesnos design system:
 * confirmDialog() from js/vendor/mesnos-dialog.js (vendored with
 * tools/update-mesnos.sh). */

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
