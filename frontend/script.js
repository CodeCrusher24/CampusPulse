/* ═══════════════════════════════════════════════
   CAMPUSPULSE — script.js
   Full frontend logic for the Spring Boot backend
═══════════════════════════════════════════════ */

const API = 'http://localhost:8080';

// ═══ STATE ═══
let token     = localStorage.getItem('cp_token') || null;
let currentUser = JSON.parse(localStorage.getItem('cp_user') || 'null');
let allCategories = [];
let allVenues     = [];

// Pagination state
const pages = {
  events:    0, myEvents: 0, allEvents: 0,
  pending:   0, users:    0
};

/* ══════════════════════════════════════════════
   BOOT
══════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  if (token && currentUser) {
    bootApp();
  } else {
    showAuthScreen();
  }
});

function showAuthScreen() {
  document.getElementById('auth-screen').classList.add('active');
  document.getElementById('app-screen').classList.remove('active');
}

function bootApp() {
  document.getElementById('auth-screen').classList.remove('active');
  document.getElementById('app-screen').classList.add('active');

  // Set user info in sidebar
  document.getElementById('user-email-display').textContent = currentUser.email;
  document.getElementById('user-role-display').textContent  = formatRole(currentUser.role);
  document.getElementById('user-avatar').textContent        = currentUser.email[0].toUpperCase();

  buildNav();
  loadCategories();
  loadVenues();

  // Auto-navigate to first nav item
  const role = currentUser.role;
  if (role === 'STUDENT')            navigateTo('view-events');
  else if (role === 'EVENT_COORDINATOR') navigateTo('view-my-events');
  else if (role === 'FACULTY_AUTHORITY') navigateTo('view-pending');
  else if (role === 'SYSTEM_ADMIN')      navigateTo('view-all-events');
}

/* ══════════════════════════════════════════════
   NAV BUILDER
══════════════════════════════════════════════ */
function buildNav() {
  const role = currentUser.role;
  const nav  = document.getElementById('sidebar-nav');
  nav.innerHTML = '';

  const items = [];

  // All roles: published events
  items.push({ icon: '🎉', label: 'Events', view: 'view-events' });

  if (role === 'EVENT_COORDINATOR') {
    items.push({ icon: '📋', label: 'My Events', view: 'view-my-events' });
  }

  if (role === 'FACULTY_AUTHORITY') {
    items.push({ icon: '✅', label: 'Pending Approvals', view: 'view-pending' });
    items.push({ icon: '📋', label: 'All Events',        view: 'view-all-events' });
  }

  if (role === 'SYSTEM_ADMIN') {
    items.push({ icon: '📋', label: 'All Events',        view: 'view-all-events' });
    items.push({ icon: '✅', label: 'Pending Approvals', view: 'view-pending' });
    items.push({ icon: '👥', label: 'Users',             view: 'view-users' });
  }

  items.forEach(item => {
    const el = document.createElement('div');
    el.className = 'nav-item';
    el.innerHTML = `<span class="nav-icon">${item.icon}</span><span>${item.label}</span>`;
    el.onclick = () => navigateTo(item.view);
    el.dataset.view = item.view;
    nav.appendChild(el);
  });

  // Show create button for admin on all-events
  if (role === 'SYSTEM_ADMIN') {
    document.getElementById('all-events-create-btn').style.display = 'block';
  }
}

function navigateTo(viewId) {
  // Deactivate all views
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active-view'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  // Activate target
  const view = document.getElementById(viewId);
  if (view) view.classList.add('active-view');

  // Activate nav item
  const navItem = document.querySelector(`.nav-item[data-view="${viewId}"]`);
  if (navItem) navItem.classList.add('active');

  // Load data
  switch (viewId) {
    case 'view-events':    loadPublishedEvents(); break;
    case 'view-my-events': loadMyEvents();        break;
    case 'view-all-events':loadAllEvents();       break;
    case 'view-pending':   loadPendingApprovals();break;
    case 'view-users':     loadUsers();           break;
  }
}

/* ══════════════════════════════════════════════
   API HELPERS
══════════════════════════════════════════════ */
async function api(method, path, body = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const opts = { method, headers };
  if (body) opts.body = JSON.stringify(body);

  const res = await fetch(API + path, opts);

  if (res.status === 401) {
    doLogout();
    throw new Error('Session expired. Please log in again.');
  }

  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }

  if (!res.ok) {
    const msg = (typeof data === 'object' && (data.error || data.message)) || 'Request failed';
    throw new Error(msg);
  }
  return data;
}

async function apiForm(path, formData) {
  const headers = {};
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const res = await fetch(API + path, { method: 'POST', headers, body: formData });
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }

  if (!res.ok) {
    const msg = (typeof data === 'object' && (data.error || data.message)) || 'Request failed';
    throw new Error(msg);
  }
  return data;
}

/* ══════════════════════════════════════════════
   AUTH
══════════════════════════════════════════════ */
function showLogin() {
  document.getElementById('login-form').classList.add('active-form');
  document.getElementById('change-password-form').classList.remove('active-form');
}

function showChangePassword() {
  document.getElementById('login-form').classList.remove('active-form');
  document.getElementById('change-password-form').classList.add('active-form');
}

async function doLogin() {
  const email    = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  const errEl    = document.getElementById('login-error');

  errEl.classList.add('hidden');

  if (!email || !password) {
    showErr(errEl, 'Please enter email and password.');
    return;
  }

  try {
    const data = await api('POST', '/api/auth/login', { email, password });
    token       = data.token;
    currentUser = data.user;
    localStorage.setItem('cp_token', token);
    localStorage.setItem('cp_user', JSON.stringify(currentUser));
    bootApp();
  } catch (e) {
    // If backend blocks login because default password hasn't been changed,
    // automatically redirect to the change-password form with fields pre-filled
    if (e.message && e.message.toLowerCase().includes('must change your default password')) {
      document.getElementById('cp-email').value   = email;
      document.getElementById('cp-old').value     = password;
      document.getElementById('cp-new').value     = '';
      document.getElementById('cp-error').classList.add('hidden');
      document.getElementById('cp-success').classList.add('hidden');
      showChangePassword();
    } else {
      showErr(errEl, e.message);
    }
  }
}

async function doChangePassword() {
  const email       = document.getElementById('cp-email').value.trim();
  const oldPassword = document.getElementById('cp-old').value;
  const newPassword = document.getElementById('cp-new').value;
  const errEl       = document.getElementById('cp-error');
  const okEl        = document.getElementById('cp-success');

  errEl.classList.add('hidden');
  okEl.classList.add('hidden');

  if (!email || !oldPassword || !newPassword) {
    showErr(errEl, 'All fields are required.');
    return;
  }

  try {
    await api('POST', '/api/auth/change-password', { email, oldPassword, newPassword });
    okEl.textContent = '✅ Password changed! Redirecting to login…';
    okEl.classList.remove('hidden');
    setTimeout(() => {
      // Pre-fill email on login form so user doesn't have to type it again
      document.getElementById('login-email').value    = email;
      document.getElementById('login-password').value = '';
      showLogin();
    }, 1800);
  } catch (e) {
    showErr(errEl, e.message);
  }
}

function doLogout() {
  token       = null;
  currentUser = null;
  localStorage.removeItem('cp_token');
  localStorage.removeItem('cp_user');
  showAuthScreen();
}

/* ══════════════════════════════════════════════
   CATEGORIES & VENUES (cached)
══════════════════════════════════════════════ */
async function loadCategories() {
  try {
    allCategories = await api('GET', '/api/categories');
    populateCategorySelects();
  } catch (e) { console.warn('Could not load categories:', e.message); }
}

async function loadVenues() {
  try {
    allVenues = await api('GET', '/api/venues');
    populateVenueSelects();
  } catch (e) { console.warn('Could not load venues:', e.message); }
}

function populateCategorySelects() {
  ['ef-category1','ef-category2','ef-category3'].forEach((id, idx) => {
    const sel = document.getElementById(id);
    if (!sel) return;
    const first = sel.options[0];
    sel.innerHTML = '';
    sel.appendChild(first);
    allCategories.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.id;
      opt.textContent = c.name;
      sel.appendChild(opt);
    });
  });
}

function populateVenueSelects() {
  const sel = document.getElementById('ef-venue');
  if (!sel) return;
  const first = sel.options[0];
  sel.innerHTML = '';
  sel.appendChild(first);
  allVenues.forEach(v => {
    const opt = document.createElement('option');
    opt.value = v.id;
    opt.textContent = `${v.name} (${v.type}${v.capacity ? ', cap:'+v.capacity : ''})`;
    sel.appendChild(opt);
  });
}

/* ══════════════════════════════════════════════
   PUBLISHED EVENTS
══════════════════════════════════════════════ */
let publishedCache = [];

async function loadPublishedEvents() {
  const grid = document.getElementById('events-grid');
  grid.innerHTML = '<div class="loading-spinner">Loading events…</div>';

  try {
    const data = await api('GET', `/api/events/published?page=${pages.events}&size=12`);
    publishedCache = data.content || [];
    renderPublishedEvents(publishedCache);
    renderPagination('events-pagination', data, (p) => { pages.events = p; loadPublishedEvents(); });
  } catch (e) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>${e.message}</p></div>`;
  }
}

function filterPublishedEvents() {
  const q = document.getElementById('events-search').value.toLowerCase();
  const filtered = publishedCache.filter(e =>
    e.title.toLowerCase().includes(q) ||
    (e.description || '').toLowerCase().includes(q) ||
    (e.primaryCategory?.name || '').toLowerCase().includes(q)
  );
  renderPublishedEvents(filtered);
}

function renderPublishedEvents(events) {
  const grid = document.getElementById('events-grid');
  if (!events.length) {
    grid.innerHTML = '<div class="empty-state"><div class="empty-icon">📭</div><p>No upcoming events</p><small>Check back later!</small></div>';
    return;
  }

  grid.innerHTML = events.map(e => {
    const pct = e.capacity ? Math.min(100, Math.round((e.registeredCount / e.capacity) * 100)) : 0;
    const imgEl = e.imageUrl
      ? `<img class="event-card-img" src="${e.imageUrl}" alt="${e.title}" onerror="this.outerHTML='<div class=\\'event-card-img placeholder\\'></div>'">`
      : `<div class="event-card-img placeholder" style="background:${catColor(e.primaryCategory?.name)}">📅</div>`;

    return `
    <div class="event-card" onclick="openEventDetail(${e.id})">
      ${imgEl}
      <div class="event-card-body">
        <span class="event-card-cat">${e.primaryCategory?.name || 'General'}</span>
        <div class="event-card-title">${esc(e.title)}</div>
        <div class="event-card-meta">
          <span>📅 ${fmtDate(e.eventDateTime)}</span>
          <span>📍 ${esc(e.venue?.name || e.customLocation || 'TBD')}</span>
        </div>
      </div>
      <div class="event-card-footer">
        <div style="flex:1">
          <div style="font-size:11px;color:var(--text-muted)">
            ${e.registeredCount}/${e.capacity} registered
          </div>
          <div class="capacity-bar">
            <div class="capacity-fill" style="width:${pct}%;background:${pct > 80 ? 'var(--danger)' : 'var(--accent)'}"></div>
          </div>
        </div>
        <span class="badge badge-published" style="margin-left:12px">Live</span>
      </div>
    </div>`;
  }).join('');
}

/* ══════════════════════════════════════════════
   MY EVENTS
══════════════════════════════════════════════ */
async function loadMyEvents() {
  const list = document.getElementById('my-events-list');
  list.innerHTML = '<div class="loading-spinner">Loading…</div>';

  try {
    const data = await api('GET', `/api/events/my-events?page=${pages.myEvents}&size=10`);
    renderEventsList('my-events-list', data.content || [], true);
    renderPagination('my-events-pagination', data, (p) => { pages.myEvents = p; loadMyEvents(); });
  } catch (e) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>${e.message}</p></div>`;
  }
}

/* ══════════════════════════════════════════════
   ALL EVENTS
══════════════════════════════════════════════ */
async function loadAllEvents() {
  const list = document.getElementById('all-events-list');
  list.innerHTML = '<div class="loading-spinner">Loading…</div>';

  try {
    const data = await api('GET', `/api/events?page=${pages.allEvents}&size=10`);
    renderEventsList('all-events-list', data.content || [], currentUser.role === 'SYSTEM_ADMIN');
    renderPagination('all-events-pagination', data, (p) => { pages.allEvents = p; loadAllEvents(); });
  } catch (e) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>${e.message}</p></div>`;
  }
}

/* ══════════════════════════════════════════════
   PENDING APPROVALS
══════════════════════════════════════════════ */
async function loadPendingApprovals() {
  const list = document.getElementById('pending-list');
  list.innerHTML = '<div class="loading-spinner">Loading…</div>';

  try {
    const data = await api('GET', `/api/events/pending-approvals?page=${pages.pending}&size=10`);
    renderEventsList('pending-list', data.content || [], false, true);
    renderPagination('pending-pagination', data, (p) => { pages.pending = p; loadPendingApprovals(); });
  } catch (e) {
    list.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><p>${e.message}</p></div>`;
  }
}

/* ══════════════════════════════════════════════
   RENDER EVENTS LIST
══════════════════════════════════════════════ */
function renderEventsList(containerId, events, isOwner = false, isApprover = false) {
  const list = document.getElementById(containerId);
  if (!events.length) {
    list.innerHTML = '<div class="empty-state"><div class="empty-icon">📭</div><p>Nothing here yet</p></div>';
    return;
  }

  const role = currentUser.role;

  list.innerHTML = events.map(e => {
    const actions = buildRowActions(e, isOwner, isApprover, role);
    return `
    <div class="event-row">
      <div class="event-row-main" onclick="openEventDetail(${e.id})" style="cursor:pointer">
        <div class="event-row-title">${esc(e.title)}</div>
        <div class="event-row-meta">
          <span>📅 ${fmtDate(e.eventDateTime)}</span>
          <span>📍 ${esc(e.venue?.name || e.customLocation || 'TBD')}</span>
          <span>👥 ${e.capacity} cap</span>
          ${e.primaryCategory ? `<span>🏷 ${e.primaryCategory.name}</span>` : ''}
        </div>
      </div>
      <span class="badge badge-${e.status.toLowerCase()}">${e.status}</span>
      <div class="event-row-actions">${actions}</div>
    </div>`;
  }).join('');
}

function buildRowActions(e, isOwner, isApprover, role) {
  const btns = [];
  const s = e.status;

  // View detail
  btns.push(`<button class="btn-icon" onclick="openEventDetail(${e.id})">👁 View</button>`);

  // Creator actions
  if (isOwner || role === 'SYSTEM_ADMIN') {
    if (s === 'DRAFT' || s === 'REJECTED') {
      btns.push(`<button class="btn-icon" onclick="openEditEvent(${e.id})">✏️ Edit</button>`);
      btns.push(`<button class="btn-icon" onclick="deleteEvent(${e.id})">🗑 Delete</button>`);
    }
    if (s === 'DRAFT') {
      btns.push(`<button class="btn-secondary btn-sm" onclick="submitEvent(${e.id})">Submit</button>`);
    }
    if (s === 'APPROVED') {
      btns.push(`<button class="btn-primary btn-sm" onclick="publishEvent(${e.id})">Publish</button>`);
    }
  }

  // Approver actions
  if ((isApprover || role === 'SYSTEM_ADMIN' || role === 'FACULTY_AUTHORITY') && s === 'PENDING') {
    if (!e.venueApproved) {
      btns.push(`<button class="btn-secondary btn-sm" onclick="openApprovalModal(${e.id},'venue')">🏛 Venue</button>`);
    }
    if (!e.eventApproved) {
      btns.push(`<button class="btn-secondary btn-sm" onclick="openApprovalModal(${e.id},'event')">📋 Event</button>`);
    }
  }

  return btns.join('');
}

/* ══════════════════════════════════════════════
   EVENT DETAIL MODAL
══════════════════════════════════════════════ */
async function openEventDetail(eventId) {
  try {
    const e = await api('GET', `/api/events/${eventId}`);
    const role = currentUser.role;

    document.getElementById('detail-title').textContent = e.title;

    // Body
    const pct = e.capacity ? Math.min(100, Math.round((e.registeredCount / e.capacity) * 100)) : 0;

    let approvalHtml = '';
    if (e.status === 'PENDING' || e.status === 'APPROVED' || e.status === 'REJECTED') {
      const vGate = e.venueApproved
        ? `<div class="gate pass"><span class="gate-icon">✅</span> Venue approved${e.venueApprovedAt ? ' • ' + fmtDate(e.venueApprovedAt) : ''}</div>`
        : `<div class="gate ${e.status === 'REJECTED' ? 'fail' : 'pending'}"><span class="gate-icon">${e.status === 'REJECTED' ? '❌' : '⏳'}</span> Venue ${e.status === 'REJECTED' ? 'rejected' : 'pending'}</div>`;

      const eGate = e.eventApproved
        ? `<div class="gate pass"><span class="gate-icon">✅</span> Event approved${e.eventApprovedAt ? ' • ' + fmtDate(e.eventApprovedAt) : ''}</div>`
        : `<div class="gate ${e.status === 'REJECTED' ? 'fail' : 'pending'}"><span class="gate-icon">${e.status === 'REJECTED' ? '❌' : '⏳'}</span> Event ${e.status === 'REJECTED' ? 'rejected' : 'pending'}</div>`;

      approvalHtml = `<div class="approval-gates">${vGate}${eGate}</div>`;
    }

    if (e.rejectionReason) {
      approvalHtml += `<div class="rejection-box">❌ Rejection: ${esc(e.rejectionReason)}</div>`;
    }

    document.getElementById('detail-body').innerHTML = `
      <div class="detail-grid">
        <div class="detail-main">
          ${e.imageUrl ? `<img src="${e.imageUrl}" style="width:100%;border-radius:8px;margin-bottom:16px;max-height:220px;object-fit:cover" onerror="this.remove()">` : ''}
          <h3>Description</h3>
          <p class="detail-desc">${esc(e.description).replace(/\n/g,'<br>')}</p>
          ${approvalHtml}
        </div>
        <div class="detail-sidebar">
          <div class="detail-meta-row">
            <span class="label">Status</span>
            <span class="value"><span class="badge badge-${e.status.toLowerCase()}">${e.status}</span></span>
          </div>
          <div class="detail-meta-row">
            <span class="label">Date</span>
            <span class="value">${fmtDate(e.eventDateTime)}</span>
          </div>
          <div class="detail-meta-row">
            <span class="label">Location</span>
            <span class="value">${esc(e.venue?.name || e.customLocation || 'TBD')}</span>
          </div>
          <div class="detail-meta-row">
            <span class="label">Capacity</span>
            <span class="value">${e.registeredCount} / ${e.capacity}</span>
          </div>
          <div style="padding:8px 0">
            <div class="capacity-bar"><div class="capacity-fill" style="width:${pct}%"></div></div>
          </div>
          ${e.registrationDeadline ? `
          <div class="detail-meta-row">
            <span class="label">Reg. Deadline</span>
            <span class="value">${fmtDate(e.registrationDeadline)}</span>
          </div>` : ''}
          <div class="detail-meta-row">
            <span class="label">Category</span>
            <span class="value">${e.primaryCategory?.name || '—'}</span>
          </div>
          ${e.tags ? `
          <div class="detail-meta-row">
            <span class="label">Tags</span>
            <span class="value">${esc(e.tags)}</span>
          </div>` : ''}
          <div class="detail-meta-row">
            <span class="label">Created by</span>
            <span class="value">${esc(e.createdBy?.email || '—')}</span>
          </div>
        </div>
      </div>`;

    // Footer actions
    const footerBtns = [];
    const s = e.status;
    const isOwner = e.createdBy?.id === currentUser.id || role === 'SYSTEM_ADMIN';

    if (isOwner && (s === 'DRAFT' || s === 'REJECTED')) {
      footerBtns.push(`<button class="btn-secondary" onclick="closeModal('modal-event-detail');openEditEvent(${e.id})">✏️ Edit</button>`);
      footerBtns.push(`<button class="btn-danger" onclick="closeModal('modal-event-detail');deleteEvent(${e.id})">🗑 Delete</button>`);
    }
    if (isOwner && s === 'DRAFT') {
      footerBtns.push(`<button class="btn-primary" onclick="closeModal('modal-event-detail');submitEvent(${e.id})">Submit for Approval</button>`);
    }
    if (isOwner && s === 'APPROVED') {
      footerBtns.push(`<button class="btn-primary" onclick="closeModal('modal-event-detail');publishEvent(${e.id})">🚀 Publish</button>`);
    }
    if ((role === 'FACULTY_AUTHORITY' || role === 'SYSTEM_ADMIN') && s === 'PENDING') {
      if (!e.venueApproved) {
        footerBtns.push(`<button class="btn-secondary" onclick="closeModal('modal-event-detail');openApprovalModal(${e.id},'venue')">🏛 Approve Venue</button>`);
      }
      if (!e.eventApproved) {
        footerBtns.push(`<button class="btn-secondary" onclick="closeModal('modal-event-detail');openApprovalModal(${e.id},'event')">📋 Approve Event</button>`);
      }
    }

    document.getElementById('detail-footer').innerHTML = footerBtns.join('');

    openModal('modal-event-detail');
  } catch (e) {
    showToast(e.message, 'error');
  }
}

/* ══════════════════════════════════════════════
   CREATE / EDIT EVENT
══════════════════════════════════════════════ */
function openCreateEvent() {
  document.getElementById('event-form-title').textContent = 'Create Event';
  document.getElementById('ef-id').value = '';
  document.getElementById('event-form').reset();
  document.getElementById('event-form-error').classList.add('hidden');
  populateCategorySelects();
  populateVenueSelects();
  openModal('modal-event-form');
}

async function openEditEvent(eventId) {
  try {
    const e = await api('GET', `/api/events/${eventId}`);

    document.getElementById('event-form-title').textContent = 'Edit Event';
    document.getElementById('ef-id').value = e.id;
    document.getElementById('ef-title').value = e.title || '';
    document.getElementById('ef-description').value = e.description || '';
    document.getElementById('ef-datetime').value = e.eventDateTime ? e.eventDateTime.slice(0,16) : '';
    document.getElementById('ef-deadline').value = e.registrationDeadline ? e.registrationDeadline.slice(0,16) : '';
    document.getElementById('ef-venue').value = e.venue?.id || '';
    document.getElementById('ef-custom-location').value = e.customLocation || '';
    document.getElementById('ef-capacity').value = e.capacity || '';
    document.getElementById('ef-tags').value = e.tags || '';
    document.getElementById('ef-image').value = e.imageUrl || '';

    populateCategorySelects();
    populateVenueSelects();

    // Set categories after populating
    setTimeout(() => {
      if (e.primaryCategory)   document.getElementById('ef-category1').value = e.primaryCategory.id;
      if (e.secondaryCategory) document.getElementById('ef-category2').value = e.secondaryCategory.id;
      if (e.tertiaryCategory)  document.getElementById('ef-category3').value = e.tertiaryCategory.id;
      if (e.venue)             document.getElementById('ef-venue').value = e.venue.id;
    }, 50);

    document.getElementById('event-form-error').classList.add('hidden');
    openModal('modal-event-form');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function submitEventForm() {
  const id = document.getElementById('ef-id').value;
  const errEl = document.getElementById('event-form-error');
  errEl.classList.add('hidden');

  const payload = {
    title:               document.getElementById('ef-title').value.trim(),
    description:         document.getElementById('ef-description').value.trim(),
    eventDateTime:       document.getElementById('ef-datetime').value || null,
    registrationDeadline:document.getElementById('ef-deadline').value || null,
    venueId:             document.getElementById('ef-venue').value     ? Number(document.getElementById('ef-venue').value) : null,
    customLocation:      document.getElementById('ef-custom-location').value.trim() || null,
    capacity:            document.getElementById('ef-capacity').value  ? Number(document.getElementById('ef-capacity').value) : null,
    primaryCategoryId:   document.getElementById('ef-category1').value ? Number(document.getElementById('ef-category1').value) : null,
    secondaryCategoryId: document.getElementById('ef-category2').value ? Number(document.getElementById('ef-category2').value) : null,
    tertiaryCategoryId:  document.getElementById('ef-category3').value ? Number(document.getElementById('ef-category3').value) : null,
    tags:                document.getElementById('ef-tags').value.trim() || null,
    imageUrl:            document.getElementById('ef-image').value.trim() || null,
  };

  if (!payload.title)           { showErr(errEl, 'Title is required.'); return; }
  if (!payload.description)     { showErr(errEl, 'Description is required.'); return; }
  if (!payload.eventDateTime)   { showErr(errEl, 'Event date/time is required.'); return; }
  if (!payload.capacity)        { showErr(errEl, 'Capacity is required.'); return; }
  if (!payload.primaryCategoryId){ showErr(errEl, 'Primary category is required.'); return; }

  try {
    if (id) {
      await api('PUT', `/api/events/${id}`, payload);
      showToast('Event updated!', 'success');
    } else {
      await api('POST', '/api/events', payload);
      showToast('Event created!', 'success');
    }
    closeModal('modal-event-form');
    reloadCurrentView();
  } catch (e) {
    showErr(errEl, e.message);
  }
}

/* ══════════════════════════════════════════════
   EVENT ACTIONS
══════════════════════════════════════════════ */
async function submitEvent(eventId) {
  if (!confirm('Submit this event for approval?')) return;
  try {
    await api('POST', `/api/events/${eventId}/submit`);
    showToast('Event submitted for approval!', 'success');
    reloadCurrentView();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function publishEvent(eventId) {
  if (!confirm('Publish this event? Students will be able to see it.')) return;
  try {
    await api('POST', `/api/events/${eventId}/publish`);
    showToast('Event published!', 'success');
    reloadCurrentView();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function deleteEvent(eventId) {
  if (!confirm('Delete this event? This cannot be undone.')) return;
  try {
    await api('DELETE', `/api/events/${eventId}`);
    showToast('Event deleted.', 'info');
    reloadCurrentView();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

/* ══════════════════════════════════════════════
   APPROVAL MODAL
══════════════════════════════════════════════ */
function openApprovalModal(eventId, gate) {
  document.getElementById('approve-event-id').value = eventId;
  document.getElementById('approve-gate').value     = gate;
  document.getElementById('approve-title').textContent = gate === 'venue' ? 'Venue Approval' : 'Event Approval';
  document.getElementById('approve-desc').textContent  = gate === 'venue'
    ? 'Approve or reject the venue booking for this event.'
    : 'Approve or reject this event proposal.';
  document.getElementById('approve-remarks').value = '';
  document.getElementById('approve-error').classList.add('hidden');
  openModal('modal-approve');
}

async function submitApproval(approve) {
  const eventId = document.getElementById('approve-event-id').value;
  const gate    = document.getElementById('approve-gate').value;
  const remarks = document.getElementById('approve-remarks').value.trim();
  const errEl   = document.getElementById('approve-error');

  if (!approve && !remarks) {
    showErr(errEl, 'Remarks are required when rejecting.');
    return;
  }

  const endpoint = gate === 'venue'
    ? `/api/events/${eventId}/approve/venue`
    : `/api/events/${eventId}/approve/event`;

  try {
    await api('POST', endpoint, { approve, remarks: remarks || null });
    showToast(approve ? 'Approved!' : 'Rejected.', approve ? 'success' : 'info');
    closeModal('modal-approve');
    reloadCurrentView();
  } catch (e) {
    showErr(errEl, e.message);
  }
}

/* ══════════════════════════════════════════════
   USER MANAGEMENT
══════════════════════════════════════════════ */
async function loadUsers() {
  const tbody = document.getElementById('users-tbody');
  tbody.innerHTML = '<tr><td colspan="5" class="loading-spinner">Loading…</td></tr>';

  try {
    const data = await api('GET', `/api/admin/users?page=${pages.users}&size=20`);
    const users = data.content || [];

    if (!users.length) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No users found</td></tr>';
      return;
    }

    tbody.innerHTML = users.map(u => {
      const active = u.enabled && !u.deletedAt;
      const actions = buildUserActions(u, active);
      return `<tr>
        <td>${esc(u.email)}</td>
        <td><span class="badge badge-${roleClass(u.role)}">${formatRole(u.role)}</span></td>
        <td><span class="badge ${active ? 'badge-active' : 'badge-inactive'}">${active ? 'Active' : 'Inactive'}</span></td>
        <td>${u.mustChangePassword ? '⚠️ Yes' : '✓ No'}</td>
        <td><div class="action-btns">${actions}</div></td>
      </tr>`;
    }).join('');

    renderPagination('users-pagination', data, (p) => { pages.users = p; loadUsers(); });
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5">${e.message}</td></tr>`;
  }
}

function buildUserActions(u, active) {
  const btns = [];

  // Role change (only EC ↔ Faculty)
  if (u.role === 'EVENT_COORDINATOR' || u.role === 'FACULTY_AUTHORITY') {
    btns.push(`<button class="btn-icon" onclick="openChangeRole(${u.id},'${u.email}','${u.role}')">🔄 Role</button>`);
  }

  // Reset password
  btns.push(`<button class="btn-icon" onclick="resetPassword(${u.id})">🔑 Reset Pwd</button>`);

  // Activate / deactivate
  if (active) {
    btns.push(`<button class="btn-icon" onclick="deactivateUser(${u.id})" style="color:var(--danger)">🚫 Deactivate</button>`);
  } else {
    btns.push(`<button class="btn-icon" onclick="reactivateUser(${u.id})" style="color:var(--success)">✅ Activate</button>`);
  }

  return btns.join('');
}

function openCreateUserModal() {
  document.getElementById('new-user-email').value = '';
  document.getElementById('new-user-type').value = 'user';
  document.getElementById('create-user-error').classList.add('hidden');
  document.getElementById('create-user-success').classList.add('hidden');
  openModal('modal-create-user');
}

async function submitCreateUser() {
  const email = document.getElementById('new-user-email').value.trim();
  const type  = document.getElementById('new-user-type').value;
  const errEl = document.getElementById('create-user-error');
  const okEl  = document.getElementById('create-user-success');

  errEl.classList.add('hidden');
  okEl.classList.add('hidden');

  if (!email) { showErr(errEl, 'Email is required.'); return; }

  try {
    const endpoint = type === 'admin' ? '/api/admin/users/admin' : '/api/admin/users';
    const user = await api('POST', endpoint, { email });
    okEl.textContent = `Created! Default password: ${email.split('@')[0]}`;
    okEl.classList.remove('hidden');
    loadUsers();
    setTimeout(() => closeModal('modal-create-user'), 2000);
  } catch (e) {
    showErr(errEl, e.message);
  }
}

function openBulkImportModal() {
  document.getElementById('bulk-csv-file').value = '';
  document.getElementById('bulk-error').classList.add('hidden');
  document.getElementById('bulk-success').classList.add('hidden');
  openModal('modal-bulk-import');
}

async function submitBulkImport() {
  const file  = document.getElementById('bulk-csv-file').files[0];
  const errEl = document.getElementById('bulk-error');
  const okEl  = document.getElementById('bulk-success');

  errEl.classList.add('hidden');
  okEl.classList.add('hidden');

  if (!file) { showErr(errEl, 'Please select a CSV file.'); return; }

  try {
    const fd = new FormData();
    fd.append('file', file);
    const result = await apiForm('/api/admin/users/bulk', fd);
    okEl.textContent = result.message || `Imported ${result.usersCreated} users!`;
    okEl.classList.remove('hidden');
    loadUsers();
    setTimeout(() => closeModal('modal-bulk-import'), 2500);
  } catch (e) {
    showErr(errEl, e.message);
  }
}

function openChangeRole(userId, email, currentRole) {
  document.getElementById('role-user-id').value          = userId;
  document.getElementById('role-user-email-display').textContent = `Changing role for: ${email}`;
  document.getElementById('new-role-select').value       = currentRole === 'EVENT_COORDINATOR' ? 'FACULTY_AUTHORITY' : 'EVENT_COORDINATOR';
  document.getElementById('role-error').classList.add('hidden');
  openModal('modal-change-role');
}

async function submitRoleChange() {
  const id      = document.getElementById('role-user-id').value;
  const newRole = document.getElementById('new-role-select').value;
  const errEl   = document.getElementById('role-error');
  errEl.classList.add('hidden');

  try {
    await api('PUT', `/api/admin/users/${id}/role`, { newRole });
    showToast('Role updated!', 'success');
    closeModal('modal-change-role');
    loadUsers();
  } catch (e) {
    showErr(errEl, e.message);
  }
}

async function resetPassword(userId) {
  if (!confirm('Reset this user\'s password to their default (email prefix)?')) return;
  try {
    await api('POST', `/api/admin/users/${userId}/reset-password`);
    showToast('Password reset. User must change on next login.', 'success');
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function deactivateUser(userId) {
  if (!confirm('Deactivate this account?')) return;
  try {
    await api('PUT', `/api/admin/users/${userId}/deactivate`);
    showToast('User deactivated.', 'info');
    loadUsers();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function reactivateUser(userId) {
  if (!confirm('Reactivate this account?')) return;
  try {
    await api('PUT', `/api/admin/users/${userId}/reactivate`);
    showToast('User reactivated!', 'success');
    loadUsers();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

/* ══════════════════════════════════════════════
   PAGINATION HELPER
══════════════════════════════════════════════ */
function renderPagination(containerId, pageData, onPageChange) {
  const el = document.getElementById(containerId);
  if (!el || !pageData) return;

  const total = pageData.totalPages || 0;
  const cur   = pageData.number    || 0;

  if (total <= 1) { el.innerHTML = ''; return; }

  let html = '';

  if (cur > 0) {
    html += `<button class="page-btn" onclick="(${onPageChange})(${cur-1})">← Prev</button>`;
  }

  for (let i = Math.max(0, cur-2); i <= Math.min(total-1, cur+2); i++) {
    html += `<button class="page-btn ${i === cur ? 'current' : ''}" onclick="(${onPageChange})(${i})">${i+1}</button>`;
  }

  if (cur < total - 1) {
    html += `<button class="page-btn" onclick="(${onPageChange})(${cur+1})">Next →</button>`;
  }

  el.innerHTML = html;
}

/* ══════════════════════════════════════════════
   RELOAD CURRENT VIEW
══════════════════════════════════════════════ */
function reloadCurrentView() {
  const active = document.querySelector('.view.active-view');
  if (!active) return;
  switch(active.id) {
    case 'view-events':     loadPublishedEvents();  break;
    case 'view-my-events':  loadMyEvents();         break;
    case 'view-all-events': loadAllEvents();        break;
    case 'view-pending':    loadPendingApprovals(); break;
    case 'view-users':      loadUsers();            break;
  }
}

/* ══════════════════════════════════════════════
   MODAL HELPERS
══════════════════════════════════════════════ */
function openModal(id) {
  document.getElementById(id).classList.remove('hidden');
}

function closeModal(id) {
  document.getElementById(id).classList.add('hidden');
}

// Close on overlay click
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) overlay.classList.add('hidden');
  });
});

/* ══════════════════════════════════════════════
   TOAST
══════════════════════════════════════════════ */
let toastTimer;

function showToast(msg, type = 'info') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className   = `toast ${type}`;
  t.classList.remove('hidden');

  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.add('hidden'), 3500);
}

/* ══════════════════════════════════════════════
   UTILITIES
══════════════════════════════════════════════ */
function showErr(el, msg) {
  el.textContent = msg;
  el.classList.remove('hidden');
}

function esc(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function fmtDate(dt) {
  if (!dt) return '—';
  const d = new Date(dt);
  return d.toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', hour12: true
  });
}

function formatRole(role) {
  const map = {
    STUDENT:           'Student',
    EVENT_COORDINATOR: 'Event Coordinator',
    FACULTY_AUTHORITY: 'Faculty Authority',
    SYSTEM_ADMIN:      'System Admin'
  };
  return map[role] || role;
}

function roleClass(role) {
  const map = {
    STUDENT:           'published',
    EVENT_COORDINATOR: 'approved',
    FACULTY_AUTHORITY: 'pending',
    SYSTEM_ADMIN:      'rejected'
  };
  return map[role] || 'draft';
}
function togglePassword(inputId, button) {
  const input = document.getElementById(inputId);

  if (input.type === "password") {
    input.type = "text";
    button.textContent = "🙈";
  } else {
    input.type = "password";
    button.textContent = "👁";
  }
}
function catColor(name) {
  const colors = {
    'Technical':'#1c2030','Cultural':'#221c30','Sports':'#1c2a1c',
    'Academic':'#1c2430','Workshop':'#2a1c1c','Seminar':'#241c2a',
    'Social':'#1c2a2a','Competition':'#2a2a1c','Exhibition':'#2a1c2a',
  };
  return colors[name] || '#1c2030';
}

/* Enter key on auth forms */
document.getElementById('login-password').addEventListener('keydown', e => {
  if (e.key === 'Enter') doLogin();
});
document.getElementById('cp-new').addEventListener('keydown', e => {
  if (e.key === 'Enter') doChangePassword();
});