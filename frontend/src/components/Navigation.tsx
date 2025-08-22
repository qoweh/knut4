import { Link } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import styles from './Navigation.module.css';

const Navigation = () => {
  const { isAuthenticated, user } = useAuthStore();

  return (
    <nav className={styles.nav}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          Knut4
        </Link>
        <div className={styles.links}>
          <Link to="/" className={styles.link}>
            Home
          </Link>
          {isAuthenticated ? (
            <>
              <Link to="/recommendations" className={styles.link}>
                Recommendations
              </Link>
              <Link to="/me" className={styles.link}>
                Profile ({user?.name})
              </Link>
            </>
          ) : (
            <>
              <Link to="/auth/login" className={styles.link}>
                Login
              </Link>
              <Link to="/auth/signup" className={styles.link}>
                Sign Up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
