package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired // 만약, framework를 Spring이 아니라, 다른 Framework로 바꾼다고 하며느 @PersistencContext로 바꿔줘야 함
			// @Autowired는 Spring이 제공하는 에노테이션이다.
	EntityManager em;


	@Test
	void contextLoads() {

		Hello hello = new Hello();
		em.persist(hello);

		// JPA를 사용하기 위해서는, EntityManger를 사용했던 것처럼, Querydsl을 사용하려먼
		// JPAQueryFactory를 사용해야 한다.
		JPAQueryFactory query = new JPAQueryFactory(em);
		//QHello qHello = new QHello("h"); // h는 "alias"로서, 꼭 넣어 줘야 한다.
		QHello qHello = QHello.hello; // == QHello qHello = new QHello("h")

		Hello result = query
				.selectFrom(qHello) // Querydsl에서 [쿼리]와 관련된 것은 엔티티가 아니라, Q Type을 들고 와야 한다.
				.fetchOne();

		assertThat(result).isEqualTo(hello);


	}

}
