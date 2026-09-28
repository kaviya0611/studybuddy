import React, { useState, useEffect } from 'react';
import {
  getGroups,
  getGroupMembers,
  removeMember,
  handleApiError,
} from '../services/api';
import AlertMessage from '../components/AlertMessage';

export default function RemoveMember({ activeStudent, onMembershipChanged }) {
  const [groups, setGroups] = useState([]);
  const [selectedGroupId, setSelectedGroupId] = useState('');
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [members, setMembers] = useState([]);
  const [loadingGroups, setLoadingGroups] = useState(true);
  const [loadingMembers, setLoadingMembers] = useState(false);
  const [removingMemberId, setRemovingMemberId] = useState(null);
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    loadGroups();
  }, []);

  useEffect(() => {
    if (selectedGroupId) {
      const grp = groups.find((g) => g.id.toString() === selectedGroupId.toString());
      setSelectedGroup(grp || null);
      loadMembers(selectedGroupId);
    } else {
      setSelectedGroup(null);
      setMembers([]);
    }
  }, [selectedGroupId, groups]);

  const loadGroups = async () => {
    try {
      setLoadingGroups(true);
      const data = await getGroups();
      setGroups(data || []);
      if (data && data.length > 0 && !selectedGroupId) {
        setSelectedGroupId(data[0].id.toString());
      }
    } catch (err) {
      setAlert({
        type: 'error',
        message: 'Failed to load study groups: ' + handleApiError(err),
      });
    } finally {
      setLoadingGroups(false);
    }
  };

  const loadMembers = async (groupId) => {
    try {
      setLoadingMembers(true);
      const data = await getGroupMembers(groupId);
      setMembers(data || []);
    } catch (err) {
      setAlert({
        type: 'error',
        message: 'Failed to load group members: ' + handleApiError(err),
      });
    } finally {
      setLoadingMembers(false);
    }
  };

  const handleRemove = async (member) => {
    if (!activeStudent || !activeStudent.id) {
      setAlert({ type: 'error', message: 'No active student selected.' });
      return;
    }

    if (!confirm(`Are you sure you want to remove ${member.studentName} from this group?`)) {
      return;
    }

    try {
      setRemovingMemberId(member.studentId);
      setAlert(null);
      // Pass activeStudent.id as creatorId parameter or header token
      await removeMember(selectedGroupId, member.studentId, activeStudent.id);
      setAlert({
        type: 'success',
        message: `Member "${member.studentName}" was successfully removed by creator from the group.`,
      });
      // Refresh member list and groups
      await loadMembers(selectedGroupId);
      await loadGroups();
      if (onMembershipChanged) onMembershipChanged();
    } catch (err) {
      setAlert({ type: 'error', message: handleApiError(err) });
    } finally {
      setRemovingMemberId(null);
    }
  };

  const isCurrentStudentCreator =
    activeStudent && selectedGroup && selectedGroup.creatorId === activeStudent.id;

  return (
    <div>
      <div className="section-header">
        <h2>Compulsory Feature 5: Remove Member</h2>
        <p>
          Only the verified group creator has authority to remove members from a study group.
        </p>
      </div>

      <AlertMessage
        type={alert?.type}
        message={alert?.message}
        onClose={() => setAlert(null)}
      />

      <div style={{ maxWidth: '520px', marginBottom: '24px' }}>
        <label className="form-label" htmlFor="groupSelect">
          Select Study Group to Manage
        </label>
        <select
          id="groupSelect"
          className="form-control"
          value={selectedGroupId}
          onChange={(e) => setSelectedGroupId(e.target.value)}
          disabled={loadingGroups}
        >
          <option value="">-- Choose Study Group --</option>
          {groups.map((grp) => (
            <option key={grp.id} value={grp.id}>
              {grp.groupName} ({grp.subjectCode}) — Creator: {grp.creatorName}
            </option>
          ))}
        </select>
      </div>

      {selectedGroup && (
        <div
          style={{
            background: isCurrentStudentCreator ? '#eff6ff' : '#fffbeb',
            border: `1px solid ${isCurrentStudentCreator ? '#93c5fd' : '#fde68a'}`,
            borderRadius: '12px',
            padding: '16px 20px',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '12px',
          }}
        >
          <div>
            <div style={{ fontWeight: 700, color: '#1e3a8a' }}>
              Group: {selectedGroup.groupName}
            </div>
            <div style={{ fontSize: '0.88rem', color: '#475569', marginTop: '2px' }}>
              Subject: {selectedGroup.subjectName} | Official Creator:{' '}
              <strong>{selectedGroup.creatorName}</strong> ({selectedGroup.creatorEmail})
            </div>
          </div>

          <div>
            {isCurrentStudentCreator ? (
              <span
                style={{
                  background: '#16a34a',
                  color: 'white',
                  fontWeight: 700,
                  fontSize: '0.82rem',
                  padding: '6px 14px',
                  borderRadius: '9999px',
                }}
              >
                ✓ You are the verified creator (Authorized)
              </span>
            ) : (
              <span
                style={{
                  background: '#dc2626',
                  color: 'white',
                  fontWeight: 600,
                  fontSize: '0.82rem',
                  padding: '6px 14px',
                  borderRadius: '9999px',
                }}
              >
                🔒 Unauthorized: Switch to {selectedGroup.creatorName} to remove members
              </span>
            )}
          </div>
        </div>
      )}

      {loadingGroups || loadingMembers ? (
        <div className="loading-container">
          <div className="spinner" />
          <p>Fetching enrolled members...</p>
        </div>
      ) : !selectedGroupId ? (
        <div className="empty-state">
          <div className="empty-state-icon">👥</div>
          <h3>Select a study group</h3>
          <p>Select a group from the dropdown to view its enrolled members.</p>
        </div>
      ) : members.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📭</div>
          <h3>No members enrolled</h3>
          <p>No members found in this group.</p>
        </div>
      ) : (
        <div className="table-responsive" style={{ background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: 'var(--shadow-sm)' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Student Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Joined Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {members.map((member) => {
                const isMemberCreator = member.isCreator;
                return (
                  <tr key={member.id || member.studentId}>
                    <td style={{ fontWeight: 600 }}>{member.studentName}</td>
                    <td style={{ color: '#475569' }}>{member.studentEmail}</td>
                    <td>
                      {isMemberCreator ? (
                        <span className="creator-badge">👑 Group Creator</span>
                      ) : (
                        <span
                          style={{
                            background: '#f1f5f9',
                            color: '#475569',
                            fontSize: '0.78rem',
                            fontWeight: 600,
                            padding: '3px 8px',
                            borderRadius: '9999px',
                          }}
                        >
                          Member
                        </span>
                      )}
                    </td>
                    <td style={{ fontSize: '0.85rem' }}>
                      {member.joinedAt ? new Date(member.joinedAt).toLocaleDateString() : 'Active'}
                    </td>
                    <td>
                      {isMemberCreator ? (
                        <span style={{ fontSize: '0.82rem', color: '#94a3b8', fontStyle: 'italic' }}>
                          Cannot be removed
                        </span>
                      ) : isCurrentStudentCreator ? (
                        <button
                          id={`btn-remove-member-${member.studentId}`}
                          type="button"
                          className="btn btn-danger"
                          style={{ padding: '6px 14px', fontSize: '0.82rem' }}
                          onClick={() => handleRemove(member)}
                          disabled={removingMemberId === member.studentId}
                        >
                          {removingMemberId === member.studentId ? (
                            <>
                              <span className="spinner" /> Removing...
                            </>
                          ) : (
                            'Remove Member'
                          )}
                        </button>
                      ) : (
                        <span
                          style={{
                            fontSize: '0.82rem',
                            color: '#94a3b8',
                            fontStyle: 'italic',
                          }}
                          title="Only group creator can remove members"
                        >
                          Creator only
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
