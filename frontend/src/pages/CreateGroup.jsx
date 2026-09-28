import React, { useState, useEffect } from 'react';
import { getStudents, getSubjects, createGroup, handleApiError } from '../services/api';
import AlertMessage from '../components/AlertMessage';

export default function CreateGroup({ activeStudent, onGroupCreated }) {
  const [students, setStudents] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [groupName, setGroupName] = useState('');
  const [selectedStudentId, setSelectedStudentId] = useState('');
  const [selectedSubjectId, setSelectedSubjectId] = useState('');
  const [maxMembers, setMaxMembers] = useState(4);
  const [loading, setLoading] = useState(false);
  const [fetchingData, setFetchingData] = useState(true);
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    loadDropdownData();
  }, []);

  useEffect(() => {
    if (activeStudent && activeStudent.id) {
      setSelectedStudentId(activeStudent.id.toString());
    }
  }, [activeStudent]);

  const loadDropdownData = async () => {
    try {
      setFetchingData(true);
      const [studentsData, subjectsData] = await Promise.all([
        getStudents(),
        getSubjects(),
      ]);
      setStudents(studentsData || []);
      setSubjects(subjectsData || []);

      if (studentsData && studentsData.length > 0 && !selectedStudentId) {
        setSelectedStudentId(studentsData[0].id.toString());
      }
      if (subjectsData && subjectsData.length > 0 && !selectedSubjectId) {
        setSelectedSubjectId(subjectsData[0].id.toString());
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Failed to load students or subjects: ' + handleApiError(err) });
    } finally {
      setFetchingData(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setAlert(null);

    // Validation
    if (!groupName.trim()) {
      setAlert({ type: 'error', message: 'Please enter a study group name.' });
      return;
    }
    if (!selectedStudentId) {
      setAlert({ type: 'error', message: 'Please select a student creator.' });
      return;
    }
    if (!selectedSubjectId) {
      setAlert({ type: 'error', message: 'Please select a subject.' });
      return;
    }
    if (!maxMembers || parseInt(maxMembers, 10) <= 0) {
      setAlert({ type: 'error', message: 'Maximum member capacity must be a positive number greater than 0.' });
      return;
    }

    try {
      setLoading(true);
      const payload = {
        groupName: groupName.trim(),
        creatorId: parseInt(selectedStudentId, 10),
        subjectId: parseInt(selectedSubjectId, 10),
        maxMembers: parseInt(maxMembers, 10),
      };

      const response = await createGroup(payload);
      setAlert({
        type: 'success',
        message: `Group "${response.data.groupName}" created successfully (HTTP 201 Created)! Creator automatically added as the first member.`,
      });
      setGroupName('');
      setMaxMembers(4);
      if (onGroupCreated) onGroupCreated();
    } catch (err) {
      setAlert({ type: 'error', message: handleApiError(err) });
    } finally {
      setLoading(false);
    }
  };

  if (fetchingData) {
    return (
      <div className="loading-container">
        <div className="spinner" />
        <p>Loading students and subjects...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="section-header">
        <h2>Compulsory Feature 1: Create Group</h2>
        <p>Form a new subject-wise study group. The creator is automatically enrolled as the first member.</p>
      </div>

      <AlertMessage
        type={alert?.type}
        message={alert?.message}
        onClose={() => setAlert(null)}
      />

      <form onSubmit={handleSubmit} style={{ maxWidth: '640px' }}>
        <div className="form-group">
          <label className="form-label" htmlFor="groupName">
            Study Group Name <span className="required">*</span>
          </label>
          <input
            id="groupName"
            type="text"
            className="form-control"
            placeholder="e.g. Operating Systems Exam Cram Group"
            value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            disabled={loading}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="studentSelect">
            Creator Student <span className="required">*</span>
          </label>
          <select
            id="studentSelect"
            className="form-control"
            value={selectedStudentId}
            onChange={(e) => setSelectedStudentId(e.target.value)}
            disabled={loading}
            required
          >
            <option value="">-- Select Student Creator --</option>
            {students.map((student) => (
              <option key={student.id} value={student.id}>
                {student.name} ({student.email})
              </option>
            ))}
          </select>
          <p className="form-helper">This student will be the designated group creator and initial member.</p>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="subjectSelect">
            Subject <span className="required">*</span>
          </label>
          <select
            id="subjectSelect"
            className="form-control"
            value={selectedSubjectId}
            onChange={(e) => setSelectedSubjectId(e.target.value)}
            disabled={loading}
            required
          >
            <option value="">-- Select Subject --</option>
            {subjects.map((sub) => (
              <option key={sub.id} value={sub.id}>
                {sub.subjectName} ({sub.subjectCode})
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="maxMembers">
            Maximum Member Capacity <span className="required">*</span>
          </label>
          <input
            id="maxMembers"
            type="number"
            min="1"
            max="50"
            className="form-control"
            value={maxMembers}
            onChange={(e) => setMaxMembers(e.target.value)}
            disabled={loading}
            required
          />
          <p className="form-helper">Must be a positive integer greater than 0.</p>
        </div>

        <button
          id="btn-create-group"
          type="submit"
          className="btn btn-primary"
          disabled={loading}
        >
          {loading ? (
            <>
              <span className="spinner" /> Creating Group...
            </>
          ) : (
            'Create Group'
          )}
        </button>
      </form>
    </div>
  );
}
