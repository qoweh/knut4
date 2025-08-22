import { useAuthStore } from '../stores/authStore';
import styles from './Profile.module.css';

const Profile = () => {
  const { user, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    window.location.href = '/';
  };

  if (!user) {
    return (
      <div className={styles.container}>
        <h1 className={styles.title}>Profile</h1>
        <p>Please log in to view your profile.</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>My Profile</h1>
      <div className={styles.profile}>
        <div className={styles.field}>
          <label>Name</label>
          <p>{user.name}</p>
        </div>
        <div className={styles.field}>
          <label>Email</label>
          <p>{user.email}</p>
        </div>
        <div className={styles.field}>
          <label>User ID</label>
          <p>{user.id}</p>
        </div>
        <button onClick={handleLogout} className={styles.logoutButton}>
          Logout
        </button>
      </div>
    </div>
  );
};

export default Profile;
