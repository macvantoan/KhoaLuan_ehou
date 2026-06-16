const API = {
    BASE: 'http://localhost:8080/api',
    get: async (url, auth = false) => {
        const h = { 'Content-Type': 'application/json' };
        if (auth) h['Authorization'] = 'Bearer ' + Auth.token();
        const r = await fetch(API.BASE + url, { headers: h });
        if (r.status === 401) { Auth.logout(); return null; }
        if (!r.ok) throw new Error(await r.text());
        return r.json();
    },
    post: async (url, data, auth = false) => {
        const h = { 'Content-Type': 'application/json' };
        if (auth) h['Authorization'] = 'Bearer ' + Auth.token();
        const r = await fetch(API.BASE + url, { method: 'POST', headers: h, body: JSON.stringify(data) });
        if (r.status === 401) { Auth.logout(); return null; }
        if (!r.ok) throw new Error(await r.text());
        const text = await r.text();
        return text ? JSON.parse(text) : null;
    },
    put: async (url, data, auth = false) => {
        const h = { 'Content-Type': 'application/json' };
        if (auth) h['Authorization'] = 'Bearer ' + Auth.token();
        const r = await fetch(API.BASE + url, { method: 'PUT', headers: h, body: JSON.stringify(data) });
        if (r.status === 401) { Auth.logout(); return null; }
        if (!r.ok) throw new Error(await r.text());
        const text = await r.text();
        return text ? JSON.parse(text) : null;
    },
    delete: async (url, auth = false) => {
        const h = { 'Content-Type': 'application/json' };
        if (auth) h['Authorization'] = 'Bearer ' + Auth.token();
        const r = await fetch(API.BASE + url, { method: 'DELETE', headers: h });
        if (r.status === 401) { Auth.logout(); return null; }
        if (!r.ok) throw new Error(await r.text());
        return true;
    },
    upload: async (url, formData, auth = true) => {
        const h = {};
        if (auth) h['Authorization'] = 'Bearer ' + Auth.token();
        const r = await fetch(API.BASE + url, { method: 'POST', headers: h, body: formData });
        if (!r.ok) throw new Error(await r.text());
        const text = await r.text();
        return text ? JSON.parse(text) : null;
    }
};

const Auth = {
    token: () => localStorage.getItem('token'),
    user: () => JSON.parse(localStorage.getItem('user') || 'null'),
    isAdmin: () => { const u = Auth.user(); return u && u.role === 'ADMIN'; },
    isLoggedIn: () => !!Auth.token(),
    logout: () => { localStorage.removeItem('token'); localStorage.removeItem('user'); window.location.href = '/frontend/login.html'; }
};

const Utils = {
    fmt: (n) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n),
    stars: (r) => {
        let s = '';
        for (let i = 1; i <= 5; i++) s += `<i class="${i <= r ? 'bi bi-star-fill' : i - 0.5 <= r ? 'bi bi-star-half' : 'bi bi-star'} text-warning" style="font-size:0.8rem"></i>`;
        return s;
    },
    toast: (msg, type = 'success') => {
        let c = document.getElementById('toastContainer');
        if (!c) { c = document.createElement('div'); c.id = 'toastContainer'; c.className = 'toast-container-custom'; document.body.appendChild(c); }
        const icons = { success: 'bi-check-circle-fill text-success', error: 'bi-x-circle-fill text-danger', info: 'bi-info-circle-fill text-primary' };
        const t = document.createElement('div');
        t.className = `toast-custom ${type}`;
        t.innerHTML = `<i class="bi ${icons[type] || icons.info}"></i><span>${msg}</span>`;
        c.appendChild(t);
        setTimeout(() => t.remove(), 3500);
    },
    loading: (show) => {
        let el = document.getElementById('pageLoading');
        if (show) {
            if (!el) { el = document.createElement('div'); el.id = 'pageLoading'; el.className = 'page-loading'; el.innerHTML = '<div class="spinner-border text-primary" style="width:3rem;height:3rem"></div>'; document.body.appendChild(el); }
        } else if (el) el.remove();
    },
    timeAgo: (dateStr) => {
        const d = new Date(dateStr), now = new Date(), diff = Math.floor((now - d) / 1000);
        if (diff < 60) return 'vừa xong';
        if (diff < 3600) return Math.floor(diff/60) + ' phút trước';
        if (diff < 86400) return Math.floor(diff/3600) + ' giờ trước';
        return Math.floor(diff/86400) + ' ngày trước';
    },
    statusLabel: (s) => {
        const map = { PENDING: ['Chờ xác nhận','badge-pending'], CONFIRMED: ['Đã xác nhận','badge-confirmed'], SHIPPING: ['Đang giao','badge-shipping'], DELIVERED: ['Đã giao','badge-delivered'], CANCELLED: ['Đã hủy','badge-cancelled'] };
        return map[s] || [s, 'bg-secondary text-white'];
    }
};

const CATEGORIES = [
    { id: 0, name: 'Tất cả', icon: 'bi-grid' },
    { id: 1, name: 'iPhone', icon: 'bi-apple' },
    { id: 2, name: 'Samsung', icon: 'bi-phone' },
    { id: 3, name: 'Xiaomi', icon: 'bi-phone-fill' },
    { id: 4, name: 'OPPO', icon: 'bi-phone' },
    { id: 5, name: 'Vivo', icon: 'bi-phone' },
    { id: 6, name: 'Realme', icon: 'bi-phone' }
];
