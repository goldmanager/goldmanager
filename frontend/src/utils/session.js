export function isAuthenticated() {
  return !!sessionStorage.getItem('username');
}

export function saveSession(username, refreshAfter) {
  if (username) {
    sessionStorage.setItem('username', username);
  }
  if (refreshAfter) {
    sessionStorage.setItem('jwtRefresh', refreshAfter);
  }
}

export function clearSession() {
  sessionStorage.removeItem('username');
  sessionStorage.removeItem('jwtRefresh');
}

export function getCurrentUsername() {
  return sessionStorage.getItem('username');
}
