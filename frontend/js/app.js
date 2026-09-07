// IntraHub - Microsoft Teams Inspired Core Engine
const API_BASE = '/api';

let state = {
  token: localStorage.getItem('intrahub_token') || null,
  currentUser: JSON.parse(localStorage.getItem('intrahub_user') || 'null'),
  currentTab: 'dashboard',
  departments: [],
  employees: []
};

// API Fetch Engine
async function apiCall(endpoint, method = 'GET', data = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  const config = { method, headers };
  if (data) config.body = JSON.stringify(data);

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, config);
    const result = await res.json();
    if (!res.ok) {
      if (res.status === 401) {
        showToast('Session expired. Please sign in again.', 'danger');
        handleLogout();
      }
      throw new Error(result.message || 'API Request failed');
    }
    return result;
  } catch (err) {
    showToast(err.message, 'danger');
    throw err;
  }
}

// Toast Alert System
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<i class="fa-solid ${type === 'danger' ? 'fa-circle-exclamation' : 'fa-circle-check'}"></i> <span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Initialization
document.addEventListener('DOMContentLoaded', () => {
  if (state.token && state.currentUser) {
    showApp();
    navigateTo('dashboard');
  } else {
    showAuth();
  }
});

function showAuth() {
  document.getElementById('auth-section').style.display = 'flex';
  document.getElementById('app-root').style.display = 'none';
}

function showApp() {
  document.getElementById('auth-section').style.display = 'none';
  document.getElementById('app-root').style.display = 'flex';
  updateUserUI();
  fetchDepartments();
}

function updateUserUI() {
  if (!state.currentUser) return;
  const initials = `${state.currentUser.firstName?.[0] || ''}${state.currentUser.lastName?.[0] || ''}`.toUpperCase() || 'IH';
  
  // Header Avatar
  const avatarElem = document.getElementById('user-avatar');
  avatarElem.childNodes[0].nodeValue = initials + ' ';
  
  document.getElementById('user-display-name').innerText = `${state.currentUser.firstName} ${state.currentUser.lastName}`;
  document.getElementById('user-display-email').innerText = state.currentUser.email || '';

  // Profile View
  document.getElementById('prof-avatar').childNodes[0].nodeValue = initials + ' ';
  document.getElementById('prof-name').innerText = `${state.currentUser.firstName} ${state.currentUser.lastName}`;
  document.getElementById('prof-email').innerText = state.currentUser.email;
  document.getElementById('prof-role').innerText = state.currentUser.role || 'ROLE_EMPLOYEE';
  document.getElementById('prof-fn').value = state.currentUser.firstName || '';
  document.getElementById('prof-ln').value = state.currentUser.lastName || '';
  document.getElementById('prof-phone').value = state.currentUser.phone || '';
  document.getElementById('prof-desg').value = state.currentUser.designation || '';
}

// Auth Tab Switching
function switchAuthTab(tab) {
  document.getElementById('tab-login').classList.toggle('active', tab === 'login');
  document.getElementById('tab-register').classList.toggle('active', tab === 'register');
  document.getElementById('login-form').style.display = tab === 'login' ? 'block' : 'none';
  document.getElementById('register-form').style.display = tab === 'register' ? 'block' : 'none';
}

// Login & Register Handlers
async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;

  try {
    const res = await apiCall('/auth/login', 'POST', { email, password });
    if (res.data) {
      state.token = res.data.accessToken;
      state.currentUser = res.data.user;
      localStorage.setItem('intrahub_token', state.token);
      localStorage.setItem('intrahub_user', JSON.stringify(state.currentUser));
      showToast('Signed in successfully', 'success');
      showApp();
      navigateTo('dashboard');
    }
  } catch (err) {
    console.error(err);
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const data = {
    firstName: document.getElementById('reg-first-name').value,
    lastName: document.getElementById('reg-last-name').value,
    email: document.getElementById('reg-email').value,
    password: document.getElementById('reg-password').value,
    employeeCode: document.getElementById('reg-emp-code').value,
    designation: document.getElementById('reg-designation').value
  };

  try {
    const res = await apiCall('/auth/register', 'POST', data);
    if (res.data) {
      state.token = res.data.accessToken;
      state.currentUser = res.data.user;
      localStorage.setItem('intrahub_token', state.token);
      localStorage.setItem('intrahub_user', JSON.stringify(state.currentUser));
      showToast('Registration complete', 'success');
      showApp();
      navigateTo('dashboard');
    }
  } catch (err) {
    console.error(err);
  }
}

function handleLogout() {
  state.token = null;
  state.currentUser = null;
  localStorage.removeItem('intrahub_token');
  localStorage.removeItem('intrahub_user');
  showAuth();
}

// Navigation Engine
function navigateTo(tab) {
  state.currentTab = tab;

  // Rail highlight
  ['dashboard', 'employees', 'departments', 'leaves', 'profile'].forEach(t => {
    const railItem = document.getElementById(`rail-${t}`);
    if (railItem) railItem.classList.toggle('active', t === tab);

    const view = document.getElementById(`view-${t}`);
    if (view) view.style.display = t === tab ? 'block' : 'none';
  });

  // Update Subnav Header
  const titles = {
    dashboard: 'Dashboard',
    employees: 'Employee Directory',
    departments: 'Departments',
    leaves: 'Leave Portal',
    profile: 'My Profile'
  };
  document.getElementById('subnav-header-title').innerText = titles[tab] || 'IntraHub';

  if (tab === 'dashboard') loadDashboardData();
  if (tab === 'employees') fetchEmployees();
  if (tab === 'departments') fetchDepartments();
  if (tab === 'leaves') fetchLeavesData();
  if (tab === 'profile') fetchCurrentProfile();
}

function handleGlobalSearch(e) {
  const query = e.target.value;
  if (state.currentTab !== 'employees') {
    navigateTo('employees');
  }
  document.getElementById('emp-search').value = query;
  fetchEmployees();
}

// Dashboard Data
async function loadDashboardData() {
  try {
    const empRes = await apiCall('/employees?size=1');
    const deptRes = await apiCall('/departments');

    if (empRes.data) document.getElementById('metric-emp-count').innerText = empRes.data.totalElements || empRes.data.content?.length || 0;
    if (deptRes.data) document.getElementById('metric-dept-count').innerText = deptRes.data.length || 0;
  } catch (err) {
    console.error(err);
  }
}

// Department Functions
async function fetchDepartments() {
  try {
    const res = await apiCall('/departments');
    if (res.data) {
      state.departments = res.data;
      renderDepartments(res.data);
      populateDeptDropdowns();
    }
  } catch (err) {
    console.error(err);
  }
}

function renderDepartments(depts) {
  const grid = document.getElementById('dept-grid');
  if (!depts || depts.length === 0) {
    grid.innerHTML = '<p style="color: var(--text-muted); grid-column: 1/-1;">No departments registered.</p>';
    return;
  }

  grid.innerHTML = depts.map(d => `
    <div class="fluent-card">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
        <h3 style="font-size: 16px; font-weight: 700;">${d.name}</h3>
        <span class="role-pill">${d.employeeCount || 0} Staff</span>
      </div>
      <p style="font-size: 12px; color: var(--text-secondary); margin-bottom: 12px; min-height: 32px;">${d.description || 'No description.'}</p>
      <div style="font-size: 12px; color: var(--theme-dark); margin-bottom: 12px;">
        <i class="fa-solid fa-user-tie"></i> Head: <strong>${d.headName || 'Unassigned'}</strong>
      </div>
      <div style="display: flex; gap: 8px;">
        <button class="btn-fluent btn-fluent-secondary" style="height: 28px; font-size: 11px;" onclick="openEditDeptModal(${d.id}, '${d.name}', '${d.headName || ''}', '${d.description || ''}')"><i class="fa-solid fa-pen"></i> Edit</button>
        <button class="btn-fluent btn-fluent-danger" style="height: 28px; font-size: 11px;" onclick="deleteDepartment(${d.id})"><i class="fa-solid fa-trash"></i> Delete</button>
      </div>
    </div>
  `).join('');
}

function populateDeptDropdowns() {
  const options = state.departments.map(d => `<option value="${d.id}">${d.name}</option>`).join('');
  document.getElementById('emp-dept-filter').innerHTML = '<option value="">All Departments</option>' + options;
  document.getElementById('emp-edit-dept').innerHTML = '<option value="">Unassigned</option>' + options;
}

function openCreateDeptModal() {
  document.getElementById('dept-id').value = '';
  document.getElementById('dept-name').value = '';
  document.getElementById('dept-head').value = '';
  document.getElementById('dept-desc').value = '';
  document.getElementById('dept-modal-title').innerText = 'Create Department';
  document.getElementById('dept-modal').classList.add('open');
}

function openEditDeptModal(id, name, head, desc) {
  document.getElementById('dept-id').value = id;
  document.getElementById('dept-name').value = name;
  document.getElementById('dept-head').value = head;
  document.getElementById('dept-desc').value = desc;
  document.getElementById('dept-modal-title').innerText = 'Edit Department';
  document.getElementById('dept-modal').classList.add('open');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

async function handleSaveDepartment(e) {
  e.preventDefault();
  const id = document.getElementById('dept-id').value;
  const payload = {
    name: document.getElementById('dept-name').value,
    headName: document.getElementById('dept-head').value,
    description: document.getElementById('dept-desc').value
  };

  try {
    const endpoint = id ? `/departments/${id}` : '/departments';
    const method = id ? 'PUT' : 'POST';
    await apiCall(endpoint, method, payload);
    showToast(`Department ${id ? 'updated' : 'created'}`, 'success');
    closeModal('dept-modal');
    fetchDepartments();
  } catch (err) {
    console.error(err);
  }
}

async function deleteDepartment(id) {
  if (!confirm('Are you sure you want to delete this department?')) return;
  try {
    await apiCall(`/departments/${id}`, 'DELETE');
    showToast('Department removed', 'success');
    fetchDepartments();
  } catch (err) {
    console.error(err);
  }
}

// Employee Directory Functions
async function fetchEmployees() {
  const search = document.getElementById('emp-search').value;
  const deptId = document.getElementById('emp-dept-filter').value;
  const active = document.getElementById('emp-active-filter').value;

  let query = `/employees?search=${encodeURIComponent(search)}`;
  if (deptId) query += `&departmentId=${deptId}`;
  if (active !== '') query += `&active=${active}`;

  try {
    const res = await apiCall(query);
    if (res.data) {
      const employees = res.data.content || res.data;
      state.employees = employees;
      renderEmployees(employees);
    }
  } catch (err) {
    console.error(err);
  }
}

function renderEmployees(list) {
  const tbody = document.getElementById('emp-table-body');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px;">No records matching search filter.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(emp => `
    <tr>
      <td><span style="font-family: monospace; font-weight: 700; color: var(--theme-dark);">${emp.employeeCode || 'EMP-00' + emp.id}</span></td>
      <td>
        <div style="display: flex; align-items: center; gap: 10px;">
          <div class="user-avatar-header" style="width: 28px; height: 28px; font-size: 11px;">
            ${emp.firstName?.[0] || ''}${emp.lastName?.[0] || ''}
          </div>
          <div>
            <div style="font-weight: 600;">${emp.firstName} ${emp.lastName}</div>
            <div style="font-size: 11px; color: var(--text-muted);">${emp.email}</div>
          </div>
        </div>
      </td>
      <td>${emp.designation || 'Staff Member'}</td>
      <td>${emp.departmentName || '<span style="color: var(--text-muted);">Unassigned</span>'}</td>
      <td><span class="role-pill">${emp.role}</span></td>
      <td>
        <span class="status-pill ${emp.active ? 'status-active' : 'status-inactive'}">
          <i class="fa-solid fa-circle" style="font-size: 6px;"></i> ${emp.active ? 'Active' : 'Inactive'}
        </span>
      </td>
      <td>
        <button class="btn-fluent btn-fluent-secondary" style="height: 26px; padding: 0 8px; font-size: 11px;" onclick="openEditEmployeeModal(${emp.id})"><i class="fa-solid fa-pen"></i> Edit</button>
      </td>
    </tr>
  `).join('');
}

function openEditEmployeeModal(id) {
  const emp = state.employees.find(e => e.id === id);
  if (!emp) return;

  document.getElementById('emp-edit-id').value = emp.id;
  document.getElementById('emp-edit-fn').value = emp.firstName;
  document.getElementById('emp-edit-ln').value = emp.lastName;
  document.getElementById('emp-edit-desg').value = emp.designation || '';
  document.getElementById('emp-edit-dept').value = emp.departmentId || '';
  document.getElementById('emp-modal').classList.add('open');
}

async function handleSaveEmployee(e) {
  e.preventDefault();
  const id = document.getElementById('emp-edit-id').value;
  const deptId = document.getElementById('emp-edit-dept').value;

  const payload = {
    firstName: document.getElementById('emp-edit-fn').value,
    lastName: document.getElementById('emp-edit-ln').value,
    designation: document.getElementById('emp-edit-desg').value,
    departmentId: deptId ? parseInt(deptId) : null
  };

  try {
    await apiCall(`/employees/${id}`, 'PUT', payload);
    showToast('Employee details updated', 'success');
    closeModal('emp-modal');
    fetchEmployees();
  } catch (err) {
    console.error(err);
  }
}

// Profile Functions
async function fetchCurrentProfile() {
  try {
    const res = await apiCall('/auth/me');
    if (res.data) {
      state.currentUser = res.data;
      localStorage.setItem('intrahub_user', JSON.stringify(res.data));
      updateUserUI();
    }
  } catch (err) {
    console.error(err);
  }
}

async function handleProfileUpdate(e) {
  e.preventDefault();
  if (!state.currentUser?.id) return;

  const payload = {
    firstName: document.getElementById('prof-fn').value,
    lastName: document.getElementById('prof-ln').value,
    phone: document.getElementById('prof-phone').value,
    designation: document.getElementById('prof-desg').value
  };

  try {
    const res = await apiCall(`/employees/${state.currentUser.id}`, 'PUT', payload);
    if (res.data) {
      showToast('Profile updated', 'success');
      fetchCurrentProfile();
    }
  } catch (err) {
    console.error(err);
  }
}

// Leave Management Functions
async function fetchLeavesData() {
  fetchMyProfileBalances();
  fetchMyLeaveHistory();
  fetchPendingApprovals();
}

async function fetchMyProfileBalances() {
  try {
    const res = await apiCall('/auth/me');
    if (res.data) {
      state.currentUser = res.data;
      localStorage.setItem('intrahub_user', JSON.stringify(res.data));
      document.getElementById('leave-metric-annual').innerText = `${res.data.leaveBalanceAnnual ?? 15} Days`;
      document.getElementById('leave-metric-sick').innerText = `${res.data.leaveBalanceSick ?? 10} Days`;
    }
  } catch (err) {
    console.error(err);
  }
}

async function fetchMyLeaveHistory() {
  try {
    const res = await apiCall('/leaves/my-requests');
    if (res.data) {
      renderMyLeaveHistory(res.data);
    }
  } catch (err) {
    console.error(err);
  }
}

function renderMyLeaveHistory(requests) {
  const tbody = document.getElementById('leave-my-table-body');
  if (!requests || requests.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 20px;">No leave applications found.</td></tr>';
    return;
  }

  tbody.innerHTML = requests.map(req => {
    let statusClass = 'status-active';
    if (req.status === 'PENDING') statusClass = 'role-pill';
    if (req.status === 'REJECTED') statusClass = 'status-inactive';

    return `
      <tr>
        <td><span class="role-pill">${req.leaveType}</span></td>
        <td>${req.startDate}</td>
        <td>${req.endDate}</td>
        <td><strong>${req.totalDays} Day(s)</strong></td>
        <td>${req.reason || 'N/A'}</td>
        <td><span class="status-pill ${statusClass}">${req.status}</span></td>
      </tr>
    `;
  }).join('');
}

async function fetchPendingApprovals() {
  try {
    const res = await apiCall('/leaves/pending');
    if (res.data) {
      document.getElementById('leave-metric-pending').innerText = res.data.length || 0;
      renderPendingApprovals(res.data);
    }
  } catch (err) {
    console.error(err);
  }
}

function renderPendingApprovals(pendingList) {
  const tbody = document.getElementById('leave-pending-table-body');
  if (!pendingList || pendingList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 20px;">No pending manager approvals.</td></tr>';
    return;
  }

  tbody.innerHTML = pendingList.map(req => `
    <tr>
      <td>
        <div style="font-weight: 600;">${req.employeeName}</div>
        <div style="font-size: 11px; color: var(--text-muted);">${req.employeeCode}</div>
      </td>
      <td>${req.departmentName || 'Unassigned'}</td>
      <td><span class="role-pill">${req.leaveType}</span></td>
      <td>${req.startDate} to ${req.endDate}</td>
      <td><strong>${req.totalDays} Day(s)</strong></td>
      <td>${req.reason || 'No reason'}</td>
      <td>
        <div style="display: flex; gap: 6px;">
          <button class="btn-fluent btn-fluent-primary" style="height: 26px; padding: 0 8px; font-size: 11px;" onclick="processLeave(${req.id}, 'APPROVED')"><i class="fa-solid fa-check"></i> Approve</button>
          <button class="btn-fluent btn-fluent-danger" style="height: 26px; padding: 0 8px; font-size: 11px;" onclick="processLeave(${req.id}, 'REJECTED')"><i class="fa-solid fa-xmark"></i> Reject</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openApplyLeaveModal() {
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('leave-start-date').value = today;
  document.getElementById('leave-end-date').value = today;
  document.getElementById('leave-reason').value = '';
  document.getElementById('leave-modal').classList.add('open');
}

async function handleApplyLeave(e) {
  e.preventDefault();
  const payload = {
    leaveType: document.getElementById('leave-type').value,
    startDate: document.getElementById('leave-start-date').value,
    endDate: document.getElementById('leave-end-date').value,
    reason: document.getElementById('leave-reason').value
  };

  try {
    await apiCall('/leaves/apply', 'POST', payload);
    showToast('Leave application submitted!', 'success');
    closeModal('leave-modal');
    fetchLeavesData();
  } catch (err) {
    console.error(err);
  }
}

async function processLeave(id, status) {
  let rejectionReason = null;
  if (status === 'REJECTED') {
    rejectionReason = prompt('Reason for rejection (optional):');
  }

  try {
    await apiCall(`/leaves/${id}/process`, 'PUT', { status, rejectionReason });
    showToast(`Leave request ${status.toLowerCase()}`, 'success');
    fetchLeavesData();
  } catch (err) {
    console.error(err);
  }
}
