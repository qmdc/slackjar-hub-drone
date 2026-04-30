import sys
import os
import json
import cv2
import numpy as np
from ultralytics import YOLO
from pathlib import Path

def load_model(model_path):
    """加载YOLO模型"""
    try:
        model = YOLO(model_path)
        return model, None
    except Exception as e:
        return None, str(e)

def detect_image(model, image_path, conf_threshold=0.25, iou_threshold=0.45):
    """检测单张图片"""
    try:
        results = model(image_path, conf=conf_threshold, iou=iou_threshold)
        return process_results(results), None
    except Exception as e:
        return None, str(e)

def detect_video_frame(model, frame, conf_threshold=0.25, iou_threshold=0.45):
    """检测视频帧"""
    try:
        results = model(frame, conf=conf_threshold, iou=iou_threshold)
        return process_results(results), None
    except Exception as e:
        return None, str(e)

def process_results(results):
    """处理检测结果"""
    detections = []
    for result in results:
        boxes = result.boxes
        for box in boxes:
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            conf = float(box.conf[0])
            cls = int(box.cls[0])
            cls_name = result.names[cls] if result.names else str(cls)
            
            detections.append({
                'x1': x1,
                'y1': y1,
                'x2': x2,
                'y2': y2,
                'confidence': conf,
                'class_id': cls,
                'class_name': cls_name
            })
    return detections

def draw_detections(image, detections):
    """在图像上绘制检测框"""
    for det in detections:
        x1, y1, x2, y2 = int(det['x1']), int(det['y1']), int(det['x2']), int(det['y2'])
        conf = det['confidence']
        cls_name = det['class_name']
        
        cv2.rectangle(image, (x1, y1), (x2, y2), (0, 255, 0), 2)
        label = f"{cls_name}: {conf:.2f}"
        cv2.putText(image, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
    return image

def save_result_image(image_path, detections, output_path):
    """保存检测结果图片"""
    image = cv2.imread(image_path)
    image = draw_detections(image, detections)
    cv2.imwrite(output_path, image)

def process_video(input_path, output_path, model, conf_threshold=0.25, iou_threshold=0.45):
    """处理视频文件"""
    cap = cv2.VideoCapture(input_path)
    fps = cap.get(cv2.CAP_PROP_FPS)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))
    
    frame_count = 0
    total_detections = []
    
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        
        detections, _ = detect_video_frame(model, frame, conf_threshold, iou_threshold)
        if detections:
            frame = draw_detections(frame, detections)
            for det in detections:
                det['frame'] = frame_count
                total_detections.append(det)
        
        out.write(frame)
        frame_count += 1
    
    cap.release()
    out.release()
    
    return total_detections

def main():
    if len(sys.argv) < 2:
        print(json.dumps({'error': '缺少参数'}))
        return
    
    command = sys.argv[1]
    
    if command == 'load_model':
        if len(sys.argv) < 3:
            print(json.dumps({'error': '缺少模型路径'}))
            return
        model_path = sys.argv[2]
        model, error = load_model(model_path)
        if error:
            print(json.dumps({'success': False, 'error': error}))
        else:
            print(json.dumps({'success': True, 'message': '模型加载成功'}))
    
    elif command == 'detect_image':
        if len(sys.argv) < 4:
            print(json.dumps({'error': '缺少参数: image_path, model_path'}))
            return
        
        image_path = sys.argv[2]
        model_path = sys.argv[3]
        conf_threshold = float(sys.argv[4]) if len(sys.argv) > 4 else 0.25
        iou_threshold = float(sys.argv[5]) if len(sys.argv) > 5 else 0.45
        output_path = sys.argv[6] if len(sys.argv) > 6 else None
        
        model, error = load_model(model_path)
        if error:
            print(json.dumps({'success': False, 'error': error}))
            return
        
        detections, error = detect_image(model, image_path, conf_threshold, iou_threshold)
        if error:
            print(json.dumps({'success': False, 'error': error}))
        else:
            result = {'success': True, 'detections': detections}
            
            if output_path:
                save_result_image(image_path, detections, output_path)
                result['output_path'] = output_path
            
            print(json.dumps(result))
    
    elif command == 'detect_video':
        if len(sys.argv) < 5:
            print(json.dumps({'error': '缺少参数: input_path, output_path, model_path'}))
            return
        
        input_path = sys.argv[2]
        output_path = sys.argv[3]
        model_path = sys.argv[4]
        conf_threshold = float(sys.argv[5]) if len(sys.argv) > 5 else 0.25
        iou_threshold = float(sys.argv[6]) if len(sys.argv) > 6 else 0.45
        
        model, error = load_model(model_path)
        if error:
            print(json.dumps({'success': False, 'error': error}))
            return
        
        detections = process_video(input_path, output_path, model, conf_threshold, iou_threshold)
        print(json.dumps({
            'success': True,
            'detections': detections,
            'total_frames': len(set(d['frame'] for d in detections)) if detections else 0,
            'output_path': output_path
        }))
    
    elif command == 'list_models':
        model_dir = '/Users/ppsn/Documents/trae/slack-hub-drone/yolov-model'
        models = []
        
        for item in Path(model_dir).iterdir():
            if item.is_dir():
                weights_dir = item / 'weights'
                if weights_dir.exists():
                    for weight_file in weights_dir.iterdir():
                        if weight_file.suffix == '.pt':
                            models.append({
                                'name': item.name,
                                'path': str(weight_file),
                                'type': weight_file.stem
                            })
        
        print(json.dumps({'success': True, 'models': models}))
    
    elif command == 'get_stats':
        if len(sys.argv) < 3:
            print(json.dumps({'error': '缺少检测结果文件路径'}))
            return
        
        result_path = sys.argv[2]
        try:
            with open(result_path, 'r') as f:
                data = json.load(f)
            
            detections = data.get('detections', [])
            class_counts = {}
            confidences = []
            
            for det in detections:
                cls_name = det['class_name']
                class_counts[cls_name] = class_counts.get(cls_name, 0) + 1
                confidences.append(det['confidence'])
            
            stats = {
                'total_detections': len(detections),
                'class_distribution': class_counts,
                'avg_confidence': sum(confidences) / len(confidences) if confidences else 0,
                'min_confidence': min(confidences) if confidences else 0,
                'max_confidence': max(confidences) if confidences else 0
            }
            
            print(json.dumps({'success': True, 'stats': stats}))
        except Exception as e:
            print(json.dumps({'success': False, 'error': str(e)}))
    
    else:
        print(json.dumps({'error': f'未知命令: {command}'}))

if __name__ == '__main__':
    main()