import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('studybuddy_auth_token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
    config.headers['X-Auth-Token'] = token;
  }
  return config;
});

// Helper to extract response data or throw error message
export const handleApiError = (error) => {
  if (error.response && error.response.data) {
    const data = error.response.data;
    if (data.validationErrors) {
      const messages = Object.entries(data.validationErrors)
        .map(([k, v]) => `${k}: ${v}`)
        .join(', ');
      return messages || data.message || 'Validation error';
    }
    return data.message || data.error || 'Server error occurred';
  }
  return error.message || 'Network error, please check backend connection';
};

// Student APIs
export const getStudents = () => api.get('/students').then((res) => res.data.data);
export const getStudentById = (id) => api.get(`/students/${id}`).then((res) => res.data.data);
export const createStudent = (data) => api.post('/students', data).then((res) => res.data.data);
export const getStudentJoinedGroups = (studentId) =>
  api.get(`/students/${studentId}/groups`).then((res) => res.data.data);

// Subject APIs
export const getSubjects = () => api.get('/subjects').then((res) => res.data.data);
export const createSubject = (data) => api.post('/subjects', data).then((res) => res.data.data);

// Study Group APIs
export const getGroups = (subjectId = null) => {
  const url = subjectId ? `/groups?subjectId=${subjectId}` : '/groups';
  return api.get(url).then((res) => res.data.data);
};
export const getGroupById = (id) => api.get(`/groups/${id}`).then((res) => res.data.data);
export const createGroup = (data) => api.post('/groups', data).then((res) => res.data);
export const transferCreator = (groupId, newCreatorId) =>
  api.put(`/groups/${groupId}/transfer-creator`, { newCreatorId }).then((res) => res.data);

// Membership APIs
export const joinGroup = (groupId, studentId) =>
  api.post(`/groups/${groupId}/join`, { studentId }).then((res) => res.data);

export const leaveGroup = (groupId, studentId) =>
  api.delete(`/groups/${groupId}/leave?studentId=${studentId}`).then((res) => res.data);

export const getGroupMembers = (groupId) =>
  api.get(`/groups/${groupId}/members`).then((res) => res.data.data);

export const removeMember = (groupId, memberStudentId, creatorId = null) => {
  const url = creatorId
    ? `/groups/${groupId}/members/${memberStudentId}?creatorId=${creatorId}`
    : `/groups/${groupId}/members/${memberStudentId}`;
  return api.delete(url).then((res) => res.data);
};

// Auth APIs
export const loginStudent = (email) =>
  api.post('/auth/login', { email }).then((res) => res.data.data);

export default api;