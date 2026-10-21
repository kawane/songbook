/* Confirmations use the shared dialog of the mesnos design system:
 * confirmDialog() from js/vendor/mesnos-dialog.js (vendored with
 * tools/update-mesnos.sh). */

/* Fixed error banner, shared by the pages that call the API. An optional
 * link ({ href, label }) opens in a new tab, so the page keeps its state. */
export function showErrorMessage(message, link) {
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
    if (link) {
        const a = document.createElement("a");
        a.href = link.href;
        a.target = "_blank";
        a.rel = "noopener";
        a.textContent = link.label;
        box.append(" ", a);
    }
    box.hidden = false;
}

export function hideErrorMessage() {
    const box = document.getElementById("app-error");
    if (box) box.hidden = true;
}
