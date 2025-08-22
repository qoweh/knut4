import styles from './Home.module.css';

const Home = () => {
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Welcome to Knut4</h1>
      <p className={styles.description}>
        Your personalized recommendation platform
      </p>
    </div>
  );
};

export default Home;
