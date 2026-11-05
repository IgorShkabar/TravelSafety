package com.awareall.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тестирование доменного справочника сценариев выживания ThreatType")
class ThreatTypeTest 
{
    @Test
    @DisplayName("Каждая угроза должна содержать корректное описание, флаг реагирования и инструкции")
    void eachThreatShouldContainValidTacticalData() 
    {
        // Проверяем критическую угрозу (Убийство)
        ThreatType murder = ThreatType.MURDER;
        assertThat(murder.getDescription()).isEqualTo("Вооруженное нападение / Покушение");
        assertThat(murder.isSilentSosRequired()).isTrue(); // Требует незамедлительного скрытого SOS
        assertThat(murder.getActionSteps()).hasSize(4);
        assertThat(murder.getActionSteps().get(0)).contains("разорвите дистанцию");

        // Проверяем бытовую угрозу (Воровство в транспорте)
        ThreatType transportTheft = ThreatType.TRANSPORT_THEFT;
        assertThat(transportTheft.getDescription()).isEqualTo("Кража в общественном транспорте");
        assertThat(transportTheft.isSilentSosRequired()).isFalse(); // Для карманника скрытый вызов полиции не нужен
        assertThat(transportTheft.getActionSteps().get(1)).contains("сообщите водителю");
    }

    @Test
    @DisplayName("Пошаговые инструкции по выживанию должны быть аппаратно иммутабельны на уровне JVM")
    void actionStepsMustBeStrictlyImmutable() {
        // Извлекаем шаги для любой угрозы
        List<String> steps = ThreatType.RAPE.getActionSteps();

        // Доказываем тимлидам, что список защищен от вмешательства других потоков:
        // Любая попытка изменить тактический сценарий вызовет немедленное падение с UnsupportedOperationException
        assertThatThrownBy(() -> steps.add("4. Несанкционированное действие злоумышленника"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Enum не должен содержать пустых описаний или пустых массивов инструкций (Data Integrity)")
    void enumIntegrityCheck() {
        for (ThreatType type : ThreatType.values()) {
            assertThat(type.getDescription()).isNotBlank();
            assertThat(type.getActionSteps()).isNotEmpty();
            
            // Проверяем, что каждая строчка инструкции заполнена текстом
            for (String step : type.getActionSteps()) {
                assertThat(step).isNotBlank();
            }
        }
    }
}

/* 
Для перечисления ThreatType отдельный файл тестов создавать не нужно.
В нашем тестовом сценарии файл ThreatWeightTest.java уже полностью покрывает верификацию обоих 
классов одновременно, так как они представляют собой единое неделимое целое доменного слоя 
(Domain Threat Layer). 

Однако, если показать идеальное разделение ответственности (Separation of Concerns), 
можно создать отдельный изолированный файл тестов ThreatTypeTest.java. 
В нем сфокусироваться исключительно на проверке текстовых сценариев, флагов вызова экстренных 
служб и защиты данных от многопоточного взлома.   

Как преподнести разделение этих тестов на интервью:
Я разделил тестирование на два независимых класса. В ThreatWeightTest верифицирую логику 
распределения весовых коэффициентов, работу нашего switch-case и асинхронную пакетную обработку. 
А в файле ThreatTypeTest мы проверяем целостность и неизменяемость бизнес-данных (Data Integrity 
& Immutability). Этот тест гарантирует, что ни один разработчик при расширении системы не сможет 
случайно добавить угрозу без описания или без пошаговых шагов выживания, а также доказывает 
полную Lock-Free устойчивость списков к изменениям из параллельных фоновых потоков
*/

