const Cart = {
    KEY: 'phonestore_cart',
    get: () => JSON.parse(localStorage.getItem(Cart.KEY) || '[]'),
    save: (items) => { localStorage.setItem(Cart.KEY, JSON.stringify(items)); Cart.updateBadge(); },
    count: () => Cart.get().reduce((sum, i) => sum + i.qty, 0),
    total: () => Cart.get().reduce((sum, i) => sum + i.price * i.qty, 0),

    add: (product, qty = 1) => {
        const items = Cart.get();
        const idx = items.findIndex(i => i.id === product.id);
        if (idx >= 0) items[idx].qty = Math.min(items[idx].qty + qty, product.stock || 99);
        else items.push({ id: product.id, name: product.name, price: product.price, image: product.images?.[0] || product.image || '', stock: product.stock || 99, qty });
        Cart.save(items);
        Utils.toast(`Đã thêm "${product.name}" vào giỏ hàng`);
    },

    remove: (id) => { Cart.save(Cart.get().filter(i => i.id !== id)); Utils.toast('Đã xóa sản phẩm', 'info'); },

    updateQty: (id, qty) => {
        const items = Cart.get();
        const i = items.find(x => x.id === id);
        if (i) { i.qty = Math.max(1, Math.min(qty, i.stock)); }
        Cart.save(items);
    },

    clear: () => { localStorage.removeItem(Cart.KEY); Cart.updateBadge(); },

    updateBadge: () => {
        document.querySelectorAll('.cart-badge').forEach(el => {
            const c = Cart.count();
            el.textContent = c;
            el.style.display = c > 0 ? '' : 'none';
        });
    }
};

document.addEventListener('DOMContentLoaded', () => Cart.updateBadge());
