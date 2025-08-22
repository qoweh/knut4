import styles from './Recommendations.module.css';

const Recommendations = () => {
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Recommendations</h1>
      <p className={styles.description}>
        Personalized recommendations will appear here
      </p>
      <div className={styles.placeholder}>
        <p>🔍 No recommendations available yet</p>
      </div>
    </div>
  );
};

export default Recommendations;
