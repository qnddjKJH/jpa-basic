package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            for (int i = 0; i < 100; i++) {
                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                em.persist(member);
            }


            // type 이 명확한 경우
            TypedQuery<Member> query = em.createQuery(
                    "select m from Member m",
                    Member.class
            );
            // 하지만 차라리 type 이 불명확하면 TypeQuery 말고 Query 를 사용하자.
            Query query3 = em.createQuery(
                    "select m.username from Member m"
            );

            // 결과가 하나 이상일 때 (없으면 null) -
            // 요새는 옵셔널로 줘서 예외를 안터뜨리게 바뀌는 중(Spring Data JPA)
//            List<Member> resultList = query.getResultList();
            // 결과가 정확히 하나 (예외 터집니다. - Spring Data JPA 는 옵셔널로 예외 안 터짐)
//            Member singleResult = query.getSingleResult();

            // 실무에서는 보통 체인으로 묶는다
            Member result = em.createQuery("select m from Member m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();
            // 파라미터 바인딩에서 위치 기반도 존재 ?(위치 번호)
            // 하지만 번호로 하지말자 @Enumerated(EnumType.ORDINAL) 을 사용하지 않는 이유와 같다.
            // 위치 하나가 밀리면 어마어마하게 번거로워진다.

            em.flush();
            em.clear();

            String query1 = "select m from Member m order by m.age desc";
            // jpa 에서 페이징은 간단하다 api 가 제공해줌
            List<Member> resultList1 = em.createQuery(query1, Member.class)
                    .setFirstResult(1)
                    .setMaxResults(10)
                    .getResultList();

            for (Member member1 : resultList1) {
                System.out.println(member1.toString());
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }
}
